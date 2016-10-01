package org.pasr.asr.dictionary;

import org.pasr.asr.Configuration;
import org.pasr.prep.corpus.Word;
import org.pasr.prep.corpus.WordSequence;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;


/**
 * @class Dictionary
 * @brief Implements a dictionary as it is defined by CMU Sphinx
 *
 * @see <a href="http://cmusphinx.sourceforge.net/wiki/tutorialdict">http://cmusphinx.sourceforge.net/wiki/tutorialdict</a>
 */
public class Dictionary extends LinkedHashMap<String, String> {

    /**
     * @brief Default Constructor
     */
    public Dictionary () {
        unknownWords_ = new ArrayList<>();
    }

    /**
     * @brief Creates a Dictionary from an InputStream
     *
     * @param inputStream
     *     The InputStream to read data from
     *
     * @return The loaded Dictionary
     */
    public static Dictionary createFromStream (InputStream inputStream) {
        Dictionary dictionary = new Dictionary();

        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            int indexOfSeparation = line.indexOf(" ");

            dictionary.put(line.substring(0, indexOfSeparation),
                line.substring(indexOfSeparation + 1));
        }
        scanner.close();

        return dictionary;
    }

    /**
     * @brief Returns the phones of a single word
     *
     * @param string
     *     The word String
     *
     * @return The phones of the given word
     */
    private List<String> getPhones (String string) {
        String phones = get(string);

        if (phones == null) {
            return autoPronounce(string);
        }
        else {
            return Arrays.asList(phones.trim().split(" "));
        }
    }

    /**
     * @brief Returns the phones of a Word
     *
     * @param word
     *     The Word object
     *
     * @return The phones of the given Word
     */
    private List<String> getPhones (Word word) {
        return getPhones(word.toString());
    }

    public List<List<String>> getPhones (WordSequence wordSequence) {
        return wordSequence.stream()
            .map(this :: getPhones)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @brief Returns the phones of a WordSequence in a single List
     *
     * @param wordSequence
     *     The WordSequence
     *
     * @return The List containing the phones of the given WordSequence
     */
    public List<String> getPhonesInLine (WordSequence wordSequence) {
        List<String> list = new ArrayList<>();

        getPhones(wordSequence).stream()
            .forEach(list:: addAll);

        return list;
    }

    /**
     * @brief Returns the phones of all the entries of the given word
     *
     * @param key
     *     The word String
     *
     * @return The phones of all the entries of the given word
     */
    public Map<String, String> getEntriesByKey (String key) {
        if (! containsKey(key)) {
            return null;
        }

        LinkedHashMap<String, String> entryMap = new LinkedHashMap<>();
        entryMap.put(key, get(key));

        int index = 2;
        String currentKey = key + "(" + index + ")";
        while (containsKey(currentKey)) {
            entryMap.put(currentKey, get(currentKey));

            index++;
            currentKey = key + "(" + index + ")";
        }

        return entryMap;
    }

    /**
     * @brief Returns the unknown words for this Dictionary
     *
     * @return The unknown words for this Dictionary
     */
    public List<String> getUnknownWords () {
        return unknownWords_;
    }

    /**
     * @brief Returns a Set of the unique words inside this Dictionary
     *
     * @return a Set of the unique words of this Dictionary
     */
    private Set<String> getUniqueWords () {
        return keySet().stream().
            filter(entry -> ! entry.contains("(")).
            collect(Collectors.toSet());
    }

    /**
     * @brief Applies fuzzy matching on a given word
     *        Fuzzy matching will find words that are similar and not necessarily exactly the same
     *        the given word. The matching is done using the Levenshtein distance as a similarity
     *        metric.
     *
     * @param string
     *     The word to use for fuzzy matching
     * @param count
     *     The number of words to return
     *
     * @return A List with the matching words
     */
    private List<String> fuzzyMatch (String string, int count) {
        String[] bestMatches = new String[count];
        double[] bestDistances = new double[count];
        for (int i = 0; i < count; i++) {
            bestDistances[i] = Double.POSITIVE_INFINITY;
        }

        for (String word : getUniqueWords()) {
            double distance = getLevenshteinDistance(string, word);

            for (int i = 0; i < count; i++) {
                if (distance < bestDistances[i]) {
                    bestDistances[i] = distance;
                    bestMatches[i] = word;
                    break;
                }
            }
        }

        return Arrays.asList(bestMatches);
    }

    /**
     * @brief Applies fuzzy matching on a given word
     *
     * @param string
     *     The word to use for fuzzy matching
     *
     * @return The top five matches
     */
    public List<String> fuzzyMatch (String string) {
        return fuzzyMatch(string, 5);
    }

    /**
     * @brief Puts an entry on this Dictionary
     *
     * @param key
     *     The word of the entry
     * @param value
     *     The phone sequence of the entry
     *
     * @return null
     */
    @Override
    public String put (String key, String value) {
        if (! containsKey(key)) {
            super.put(key, value);
            return null;
        }

        int index = 2;
        String currentKey = key + "(" + index + ")";
        while (containsKey(currentKey)) {
            // if the given value already exists inside the dictionary, don't put it again
            if (get(currentKey).equals(value)) {
                return null;
            }

            index++;
            currentKey = key + "(" + index + ")";
        }

        super.put(currentKey, value);

        return null;
    }

    /**
     * @brief Adds the given word as an unknown word
     *
     * @param word
     *     The unknown word to be added
     */
    public void addUnknownWord (String word) {
        if (! unknownWords_.contains(word)) {
            unknownWords_.add(word);
        }
    }

    /**
     * @brief Removes all the entries for the given word
     *
     * @param key
     *     The word the entries of which to remove
     */
    public void remove (String key) {
        if (super.remove(key) == null) {
            return;
        }

        int index = 2;
        while (super.remove(key + "(" + index + ")") != null) {
            index++;
        }
    }

    /**
     * @brief Removes the given unknown word
     *
     * @param word
     *     the unknown word to be removed
     */
    public void removeUnknownWord (String word) {
        unknownWords_.remove(word);
    }

    /**
     * @brief Returns the default Dictionary based on the default ASR Configuration
     *
     * @return The default Dictionary based on the default ASR Configuration
     *
     * @throws FileNotFoundException If the default Dictionary file is not found
     */
    public static Dictionary getDefaultDictionary () throws FileNotFoundException {
        return Dictionary.createFromStream(new FileInputStream(
            Configuration.getDefaultConfiguration().getDictionaryPath()
        ));
    }

    /**
     * @brief Writes this Dictionary to an OutputStream
     *
     * @param outputStream
     *     The OutputStream to write on
     */
    public void exportToStream (OutputStream outputStream) {
        // Sort the entries of the dictionary based on the key length. This will ensure that
        // "the(1)" is below "the" when the dictionary is saved to the file.
        List<Map.Entry<String, String>> entries = new ArrayList<>(entrySet());

        Collections.sort(entries, (e1, e2) -> e1.getKey().length() - e2.getKey().length());

        PrintWriter printWriter = new PrintWriter(outputStream);
        for (Map.Entry<String, String> entry : entries) {
            printWriter.write(entry.getKey() + " " + entry.getValue() + "\n");
        }
        printWriter.close();
    }

    /**
     * @brief Creates the phones for the given word
     *
     * @param string
     *     The word the phones of which will be created
     *
     * @return A List with the created phones
     */
    public static List<String> autoPronounce (String string) {
        List<String> list = new ArrayList<>();

        for (char ch : string.trim().toUpperCase().toCharArray()) {
            list.add(String.valueOf(ch));
        }

        return list;
    }

    private final List<String> unknownWords_; //!< The unknown words for this Dictionary

}
