package org.prep;

import org.postp.TextLine;
import org.utilities.ArrayIterable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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
            String nextToken = scanner.next().
                    replaceAll(newLineDelimiter_, "").
                    replaceAll(wordSeparator_ + "+", wordSeparator_);

            if(nextToken.length() > 0 && !nextToken.equals(wordSeparator_)) {
                sentences.add(new TextLine(nextToken, removePunctuationMarks));
            }
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

    public void saveToFile(File file) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(file);

        for(TextLine sentence : sentences_){
            printWriter.write(
                    "<s>" + wordSeparator_ + sentence.toString().toLowerCase() +
                            wordSeparator_ + "</s>" + newLineDelimiter_
            );
        }

        printWriter.close();
    }

    private final TextLine[] sentences_;

    private final String wordSeparator_;
    private final String sentenceSeparator_;
    private final String newLineDelimiter_;

}
