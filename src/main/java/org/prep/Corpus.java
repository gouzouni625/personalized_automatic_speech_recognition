package org.prep;

import org.postp.TextLine;
import org.utilities.ArrayIterable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.utilities.Utilities.collectionToArray;


public class Corpus implements Iterable<TextLine> {
    public Corpus(String textCorpusPath) throws IOException {
        this(textCorpusPath, false);
    }

    public Corpus(String textCorpusPath, boolean removePunctuationMarks) throws IOException {
        this(textCorpusPath, " ", ".", "\n", removePunctuationMarks);
    }

    public Corpus(String textCorpusPath, String wordSeparator, String sentenceSeparator,
                  String newLineDelimiter, boolean removePunctuationMarks)
            throws FileNotFoundException {

        wordSeparator_ = wordSeparator;
        sentenceSeparator_ = sentenceSeparator;
        newLineDelimiter_ = newLineDelimiter;

        ArrayList<TextLine> sentences = new ArrayList<TextLine>();

        Scanner scanner = new Scanner(new File(textCorpusPath));
        scanner.useDelimiter(
                Pattern.compile(
                        Pattern.quote(wordSeparator_) + "?" +
                                Pattern.quote(sentenceSeparator_ + wordSeparator_) + "?"
                )
        );
        while (scanner.hasNext()) {
            sentences.add(new TextLine(scanner.next().
                    replaceAll(newLineDelimiter_, "").
                    replaceAll(wordSeparator_ + "+", wordSeparator_), removePunctuationMarks));
        }
        scanner.close();

        sentences_ = new TextLine[sentences.size()];
        collectionToArray(sentences, sentences_);
    }

    public TextLine[] getSentences() {
        return sentences_;
    }

    public Iterator<TextLine> iterator(){
        return (new ArrayIterable<TextLine>(sentences_).iterator());
    }

    private final TextLine[] sentences_;

    private final String wordSeparator_;
    private final String sentenceSeparator_;
    private final String newLineDelimiter_;

}
