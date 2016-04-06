package org.prep;

import org.postp.Configuration;
import org.postp.TextLine;
import org.utilities.ArrayIterable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.utilities.Utilities.collectionToArray;


public class Corpus implements Iterable<TextLine> {
    public Corpus(String textCorpusPath) throws FileNotFoundException {
        ArrayList<TextLine> sentences = new ArrayList<TextLine>();

        Scanner scanner = new Scanner(new File(textCorpusPath));
        scanner.useDelimiter(
                Pattern.compile(
                        Pattern.quote(
                                configuration_.getWordSeparator()
                        ) + "?" +
                                Pattern.quote(
                                        configuration_.getSentenceSeparator() +
                                                configuration_.getWordSeparator()
                                ) + "?"
                )
        );
        while (scanner.hasNext()) {
            String nextToken = scanner.next().
                    replaceAll("[_\\-()]+", configuration_.getWordSeparator()).
                    replaceAll("\\[.*\\]", configuration_.getWordSeparator()).
                    replaceAll(configuration_.getNewLineDelimiter(), "").
                    replaceAll(configuration_.getWordSeparator() + "+",
                            configuration_.getWordSeparator()).
                    toLowerCase();

            if(nextToken.length() > 0 && !nextToken.equals(configuration_.getWordSeparator())) {
                sentences.add(new TextLine(nextToken));
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
                    "<s>" + configuration_.getWordSeparator() + sentence.toString().toLowerCase() +
                            configuration_.getWordSeparator() + "</s>" +
                            configuration_.getNewLineDelimiter()
            );
        }

        printWriter.close();
    }

    public boolean contains(String string){
        for(TextLine sentence : sentences_){
            if(sentence.getLine().contains(string)){
                return true;
            }
        }

        return false;
    }

    private final TextLine[] sentences_;

    private final Configuration configuration_ = Configuration.getInstance();

}
