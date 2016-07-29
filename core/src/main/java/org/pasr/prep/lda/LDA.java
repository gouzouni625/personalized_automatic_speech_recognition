package org.pasr.prep.lda;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import org.pasr.utilities.Utilities;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.regex.Pattern;


public class LDA {
    public LDA(List<String> documents, int numberOfTopics, int numberOfIterations,
               int numberOfThreads){
        documents_ = documents;

        numberOfTopics_ = numberOfTopics;
        numberOfIterations_ = numberOfIterations;

        numberOfThreads_ = numberOfThreads;

        StringArrayIterator iterator = new StringArrayIterator(documents_.toArray(new String[0]));

        instances_ = new InstanceList(buildPipe());
        instances_.addThruPipe(iterator);

        // Disable the LDA logger for now.
        ParallelTopicModel.logger.setLevel(Level.OFF);
    }

    private Pipe buildPipe(){
        ArrayList<Pipe> pipeList = new ArrayList<>();

        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("[\\p{L}\\p{N}_]+")));
        pipeList.add(new TokenSequenceLowercase());
        pipeList.add(new TokenSequence2FeatureSequence());

        return new SerialPipes(pipeList);
    }

    public int getNumberOfTopics(){
        return numberOfTopics_;
    }

    public int getNumberOfIterations(){
        return numberOfIterations_;
    }

    public int getNumberOfThreads(){
        return numberOfThreads_;
    }

    public List<List<String>> getTopWords(int numberOfWords){
        Object[][] topWordsArray = lda_.getTopWords(numberOfWords);

        ArrayList<List<String>> topWordsList = new ArrayList<>();
        for(Object[] topicWordsArray : topWordsArray){
            ArrayList<String> topicWordsList = new ArrayList<>();

            for(Object topicWord : topicWordsArray){
                topicWordsList.add((String) topicWord);
            }

            topWordsList.add(topicWordsList);
        }

        return topWordsList;
    }

    public double[][] getDocumentTopicProbabilities(){
        PipedInputStream pipedInputStream = new PipedInputStream();
        PipedOutputStream pipedOutputStream;
        try {
            pipedOutputStream = new PipedOutputStream(pipedInputStream);
        } catch (IOException e) {
            // TODO Act appropriately
            e.printStackTrace();
            return null;
        }

        // Use another thread to write data to the piped output stream so that this thread doesn't
        // deadlock (See https://docs.oracle.com/javase/8/docs/api/java/io/PipedInputStream.html)
        new Thread(() -> {
            PrintWriter printWriter = new PrintWriter(pipedOutputStream);
            lda_.printDenseDocumentTopics(printWriter);
            printWriter.close();
        }).start();

        // Create a Scanner from the PipedInputStream that is connected to the PipedOutputStream
        // that was previously written by the PrintWriter
        Scanner scanner = new Scanner(pipedInputStream);

        double[][] probabilities = new double[documents_.size()][numberOfTopics_];
        int currentDocumentIndex = 0;
        while(scanner.hasNextLine()){
            String[] tokens = scanner.nextLine()
                .replaceAll("\\t", " ") // Replace tabs with spaces
                .replaceAll(" +", " ") // Collapse all series of spaces to one space
                .trim()
                .split(" ");

            for(int i = 0;i < numberOfTopics_;i++){
                probabilities[currentDocumentIndex][i] = Double.parseDouble(tokens[2 + i]);
            }

            currentDocumentIndex++;
        }

        scanner.close();

        return probabilities;
    }

    public int[] getDocumentTopic(){
        double[][] documentTopicProbabilities = getDocumentTopicProbabilities();

        int numberOfDocuments = documents_.size();
        int[] documentTopicArray = new int[numberOfDocuments];

        for(int i = 0;i < numberOfDocuments;i++){
            documentTopicArray[i] = Utilities.indexOfMax(documentTopicProbabilities[i]);
        }

        return documentTopicArray;
    }

    public void start() throws IOException {
        if(lda_ == null){
            lda_ = new ParallelTopicModel(numberOfIterations_);
        }
        else{
            lda_.setNumIterations(numberOfIterations_);
        }

        lda_.setNumTopics(numberOfTopics_);
        lda_.setNumThreads(numberOfThreads_);
        lda_.addInstances(instances_);
        lda_.estimate();
    }

    public void setNumberOfTopics(int numberOfTopics){
        numberOfTopics_ = numberOfTopics;
    }

    public void setNumberOfIterations(int numberOfIterations){
        numberOfIterations_ = numberOfIterations;
    }

    public void setNumberOfThreads(int numberOfThreads){
        numberOfThreads_ = numberOfThreads;
    }

    private List<String> documents_;
    private InstanceList instances_;

    private int numberOfTopics_;
    private int numberOfIterations_;

    private int numberOfThreads_;

    private ParallelTopicModel lda_ = null;

}
