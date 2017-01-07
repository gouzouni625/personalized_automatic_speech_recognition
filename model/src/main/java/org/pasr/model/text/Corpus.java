package org.pasr.model.text;

import org.pasr.model.asr.dictionary.Dictionary;
import org.pasr.utilities.Progress;
import org.pasr.utilities.NumberSpeller;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 * @class Corpus
 * @brief Implements a List of WordSequence objects backed by an ArrayList
 */
public class Corpus extends ArrayList<WordSequence> {

    /**
     * @brief Default Constructor
     */
    public Corpus () {
        this(null);
    }

    /**
     * @brief Constructor
     *
     * @param wordSequenceList
     *     The initial List of WordSequence objects for this Corpus
     */
    public Corpus (List<WordSequence> wordSequenceList) {
        if (wordSequenceList != null) {
            addAll(wordSequenceList);
        }

        name_ = "";
        id_ = - 1;

        progress_ = new Progress();
    }

    /**
     * @brief Returns the id of this Corpus
     *
     * @return The id of this Corpus
     */
    public int getId () {
        return id_;
    }

    /**
     * @brief Returns the name of this Corpus
     *
     * @return The name of this Corpus
     */
    public String getName () {
        return name_;
    }

    /**
     * @brief Returns the Observable Progress of this Corpus
     *        This Observable should be used to monitor the progress of the processing of a
     *        Dictionary by this Corpus
     *
     * @return The Observable Progress of this Corpus
     */
    public Observable getProgress () {
        return progress_;
    }

    /**
     * @brief Returns the number of Document objects in this Corpus
     *
     * @return The number of Document objects in this Corpus
     */
    public int numberOfDocuments () {
        return stream()
            .map(WordSequence:: getDocumentId)
            .collect(Collectors.toSet()).size();
    }

    /**
     * @brief Returns a Set of the unique Word objects inside this Corpus
     *
     * @return A Set of the unique Word objects inside this Corpus
     */
    private Set<String> getUniqueWords () {
        HashSet<String> uniqueWords = new HashSet<>();

        for (WordSequence wordSequence : this) {
            uniqueWords.addAll(wordSequence.stream()
                .map(Word:: toString)
                .collect(Collectors.toSet()));
        }

        return uniqueWords;
    }

    /**
     * @brief Processes a Dictionary to produce a reduced Dictionary
     *        The reduced Dictionary will contain only the Word objects that this Corpus contains.
     *        The progress of this process can be monitored using this Corpus Progress
     *
     * @param dictionary
     *     The Dictionary to process
     *
     * @return The reduced Dictionary
     */
    public Dictionary process (Dictionary dictionary) {
        cancelProcess_ = false;

        if (documentList_ != null && documentList_.size() > 0) {
            for (int i = 0, n = documentList_.size(); i < n; i++) {
                if (cancelProcess_) {
                    return new Dictionary();
                }

                String processedContent = processNumbers(documentList_.get(i).getContent());
                addAll(createWordSequences(
                    processedContent, documentList_.get(i).getId(), documentList_.get(i).getTitle())
                );

                progress_.setValue(((double) (i + 1)) / (2 * n));
            }
        }
        else {
            progress_.setValue(0.50);
        }

        Dictionary reducedDictionary = new Dictionary();

        Set<String> uniqueWords = getUniqueWords();
        int n = uniqueWords.size();
        int i = 0;

        for (String uniqueWord : uniqueWords) {
            if (cancelProcess_) {
                return reducedDictionary;
            }

            // There is no need to check if the dictionary contains as key
            // uniqueword(i) because in order to contain such key, it has
            // to also contain uniqueword as key.
            if (! dictionary.containsKey(uniqueWord)) {
                reducedDictionary.addUnknownWord(uniqueWord);
            }
            else {
                Map<String, String> entries = dictionary.getEntriesByKey(uniqueWord);
                if (entries != null) {
                    reducedDictionary.putAll(entries);
                }
            }

            i++;
            progress_.setValue(0.5 + ((double) (i + 1)) / (2 * n));
        }

        // Release the documents resources
        documentList_ = null;

        return reducedDictionary;
    }

    /**
     * @brief Cancels the processing of a Dictionary
     */
    public void cancelProcess () {
        cancelProcess_ = true;
    }

    /**
     * @brief Replaces any number inside this Corpus with its literal representation
     *        There are two ways that a number is pronounced. The first is for dates (e.g. 1942 ->
     *        nineteen forty two) and the second is for amounts (e.g. 1942 dollars -> one thousand
     *        nine hundred forty two dollars).
     */
    private String processNumbers (String document) {
        NumberSpeller speller = NumberSpeller.getInstance();

        ArrayList<String> matches = new ArrayList<>();

        Matcher matcher = Pattern.compile("([0-9]+) dollars").matcher(document);
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        // Sort the matches by descending length so that longer numbers get spelled first. If the
        // matches are sorted by ascending length, then a long number, say 1942 will be spelled as
        // one nine four two instead of nineteen forty two.
        Collections.sort(matches, (s1, s2) -> s2.length() - s1.length());
        for (String match : matches) {
            String spelled;
            try {
                spelled = speller.spell(Integer.valueOf(match));
            } catch (NumberFormatException e) {
                // TODO In the future, when name-entity recognition is embedded, {number} will
                // TODO accept any number.
                spelled = "number";
            }
            document = document.replaceAll(spelled + " dollars", " " + spelled + " dollars ");
        }
        matches.clear();

        matcher = Pattern.compile("([0-9]+)\\$").matcher(document);
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        Collections.sort(matches, (s1, s2) -> s2.length() - s1.length());
        for (String match : matches) {
            String spelled;
            try {
                spelled = speller.spell(Integer.valueOf(match));
            } catch (NumberFormatException e) {
                // TODO In the future, when name-entity recognition is embedded, {number} will
                // TODO accept any number.
                spelled = "number";
            }
            document = document.replaceAll(match + "\\$", " " + spelled + " dollars ");
        }
        matches.clear();

        matcher = Pattern.compile("([0-9]+)").matcher(document);
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        Collections.sort(matches, (s1, s2) -> s2.length() - s1.length());
        for (String match : matches) {
            String spelled;
            try {
                spelled = speller.spell(Integer.valueOf(match), NumberSpeller.Types.NORMAL);
            } catch (NumberFormatException e) {
                // TODO In the future, when name-entity recognition is embedded, {number} will
                // TODO accept any number.
                spelled = "number";
            }
            document = document.replaceAll(match, " " + spelled + " ");
        }

        return document;
    }

    /**
     * @brief Creates WordSequence objects from a given Document
     *        WordSequence objects are created by the sentences extracted from the give Document
     *
     * @param document
     *     The String of the Document to create the WordSequence objects from
     * @param documentID
     *     The id of the given Document
     * @param documentTitle
     *     The title of the given Document
     *
     * @return A List of WordSequence objects created from the given Document
     */
    private List<WordSequence> createWordSequences (String document, long documentID,
                                                    String documentTitle) {
        document = document.
            // From https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
            //  \p{Graph}  A visible character: [\p{Alnum}\p{Punct}]
            //    \p{Alnum}  An alphanumeric character:[\p{Alpha}\p{Digit}]
            //      \p{Alpha}  An alphabetic character:[\p{Lower}\p{Upper}]
            //        \p{Lower}  A lower-case alphabetic character: [a-z]
            //        \p{Upper}  An upper-case alphabetic character:[A-Z]
            //      \p{Digit}  A decimal digit: [0-9]
            //    \p{Punct} 	Punctuation: One of !"#$%&'()*+,-./:;<=>?@[\]^_`{|}~
                replaceAll("[^\\p{Graph}]", " ").
                replaceAll("[_\\-,:/\"<>|#@\\\\=+~*\\{}$%&`^]+", " ").
                replaceAll("[\\(\\[]", " ").
                replaceAll("[\\)\\]]", " . ").
                replaceAll("[!?;]", ".").
                replaceAll(" +", " ").
                toLowerCase();

        String[] wordSequenceTextArray = document.split(" ?\\. ?");

        ArrayList<WordSequence> wordSequences = new ArrayList<>();
        for (String wordSequenceText : wordSequenceTextArray) {
            if (! wordSequenceText.isEmpty()) {
                //noinspection MismatchedQueryAndUpdateOfCollection
                WordSequence currentWordSequence = new WordSequence(
                    wordSequenceText, documentID, documentTitle
                );

                int size = currentWordSequence.size();
                if (size <= 10) {
                    wordSequences.add(currentWordSequence);
                }
                else if (size <= 15) {
                    int half = size / 2;
                    wordSequences.add(currentWordSequence.subSequence(0, half));
                    wordSequences.add(currentWordSequence.subSequence(half, size));
                }
                else {
                    int remainder = size % 10;
                    if (remainder > 0 && remainder <= 5) {
                        wordSequences.add(currentWordSequence.subSequence(size - 6, size));
                        size -= 6;
                    }

                    // Note that size is int so, if size == 99 then size / 10 * 10 = 90
                    int n = size / 10 * 10;
                    for (int i = 0; i < n; i += 10) {
                        wordSequences.add(currentWordSequence.subSequence(i, i + 10));
                    }

                    if (n < size) {
                        wordSequences.add(currentWordSequence.subSequence(n, size));
                    }
                }
            }
        }

        return wordSequences;
    }

    /**
     * @brief Returns a List of the Document objects inside this Corpus
     *
     * @return A List of the Document Objects inside this Corpus
     */
    public List<Document> getDocuments () {
        return stream()
            .map(wordSequence -> new Document(wordSequence.getDocumentId(),
                wordSequence.getDocumentTitle(), wordSequence.toString()
            ))
            .collect(Collectors.groupingBy(Document:: getId, Collectors.toList()))
            .values().stream()
            .map(documentList -> {
                Optional<Document> r = documentList.stream()
                    .reduce((document1, document2) -> new Document(
                        document1.getId(), document1.getTitle(),
                        document1.getContent() + ".\n" + document2.getContent()
                    ));

                if (r.isPresent()) {
                    return r.get();
                }
                else {
                    return null;
                }
            })
            .filter(wordSequence -> wordSequence != null)
            .collect(Collectors.toList());
    }

    /**
     * @brief Returns the String of this Corpus
     *
     * @return The String of this Corpus
     */
    @Override
    public String toString () {
        StringBuilder stringBuilder = new StringBuilder();

        for (WordSequence wordSequence : this) {
            stringBuilder.append(wordSequence.toString()).append(".");
        }

        return stringBuilder.toString();
    }

    /**
     * @brief Returns a prettified version of the String of this Corpus
     *
     * @return A prettified version of the String of this Corpus
     */
    public String toPrettyString () {
        return toString().replaceAll("\\.", "\n");
    }

    public boolean contains (String string) {
        for (WordSequence wordSequence : this) {
            if (wordSequence.contains(string)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @brief Returns a random sub-sequence of a random WordSequence of this Corpus
     *
     * @param random
     *     The Random number generator to be used
     *
     * @return A random sub-sequence of a random WordSequence of this Corpus
     */
    public String getRandomSubSequence (Random random) {
        return get(random.nextInt(size())).getRandomSubsequence(random);
    }

    /**
     * @brief Replaces oldText with newText inside this Corpus
     *
     * @param oldText
     *     The String to be replaced
     * @param newText
     *     The String to be placed
     */
    public void replaceWordText (String oldText, String newText) {
        // If new text is empty then the words should be removed instead of having their text
        // replaced
        if (newText.isEmpty()) {
            removeWordByText(oldText);
        }
        else {
            for (WordSequence wordSequence : this) {
                wordSequence.replaceWordText(oldText, newText);
            }
        }
    }

    /**
     * @brief Removes the Word objects inside this Corpus based on their String
     *
     * @param text
     *     The String based on which the Word objects will be removed
     */
    public void removeWordByText (String text) {
        ArrayList<WordSequence> emptyWordSequences = new ArrayList<>();

        for (WordSequence wordSequence : this) {
            wordSequence.removeByText(text);

            if (wordSequence.size() == 0) {
                emptyWordSequences.add(wordSequence);
            }
        }

        emptyWordSequences.forEach(this :: remove);
    }

    /**
     * @brief Sets the id of this Corpus
     *
     * @param id
     *     The new id
     */
    public void setId (int id) {
        id_ = id;
    }

    /**
     * @brief Sets the name of this Corpus
     *
     * @param name
     *     The new Name
     */
    public void setName (String name) {
        name_ = name;
    }

    /**
     * @brief Sets the Document List of this Corpus
     *
     * @param documentList
     *     The new Document List
     */
    public void setDocuments (List<Document> documentList) {
        documentList_ = documentList;
    }

    private List<Document> documentList_; //!< A List of the Document objects of this Corpus

    private int id_; //!< The id of this Corpus
    private String name_; //!< The name of this Corpus

    private Progress progress_; //!< The Progress of this Corpus
    private volatile boolean cancelProcess_; //!< A flag indicated whether processing of a
                                             //!< Dictionary has been canceled

}
