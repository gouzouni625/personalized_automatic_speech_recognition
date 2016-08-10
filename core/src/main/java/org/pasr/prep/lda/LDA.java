package org.pasr.prep.lda;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.iterator.StringArrayIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.InstanceList;
import org.pasr.prep.corpus.Document;
import org.pasr.utilities.Utilities;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Scanner;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class LDA extends Observable {
    public LDA(List<Document> documents, int numberOfTopics, int numberOfIterations,
               int numberOfThreads){
        if(documents == null){
            throw new IllegalArgumentException("documents should not be null.");
        }

        if(documents.size() <= 1){
            throw new IllegalArgumentException("documents size should be greater than one.");
        }

        if(numberOfTopics <= 1){
            throw new IllegalArgumentException("numberOfTopics should be greater than one.");
        }

        if(numberOfIterations <= 0){
            throw new IllegalArgumentException("numberOfIterations should be greater than zero.");
        }

        if(numberOfThreads <= 0){
            throw new IllegalArgumentException("numberOfThreads should be greater than zero.");
        }

        documents_ = documents;

        numberOfTopics_ = numberOfTopics;
        numberOfIterations_ = numberOfIterations;

        numberOfThreads_ = numberOfThreads;

        createInstances(documents_);

        handleParallelTopicModelLogger();
    }

    private void createInstances(List<Document> documents){
        StringArrayIterator iterator = new StringArrayIterator(
            documents.stream()
                .map(Document :: getContent)
                .collect(Collectors.toList()).toArray(new String[0])
        );

        instances_ = new InstanceList(buildPipe());
        instances_.addThruPipe(iterator);
    }

    private Pipe buildPipe(){
        ArrayList<Pipe> pipeList = new ArrayList<>();

        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("[\\p{L}\\p{N}_]+")));
        pipeList.add(new TokenSequenceLowercase());
        pipeList.add(new TokenSequence2FeatureSequence());

        return new SerialPipes(pipeList);
    }

    private void handleParallelTopicModelLogger(){
        Logger logger = ParallelTopicModel.logger;

        logger.setUseParentHandlers(false);

        for(Handler handler : logger.getHandlers()){
            logger.removeHandler(handler);
        }

        logger.addHandler(new Handler() {
            @Override
            public void publish (LogRecord record) {
                Matcher matcher = Pattern.compile("<([0-9]+)> LL/token: .*")
                    .matcher(record.getMessage());

                if(matcher.matches()){
                    setChanged();
                    // group(1) is guaranteed to be a parsable double because of the pattern
                    notifyObservers(Double.parseDouble(matcher.group(1)) / numberOfIterations_);
                }
            }

            @Override
            public void flush () {}

            @Override
            public void close () throws SecurityException {}
        });
    }

    public int getNumberOfTopics(){
        return numberOfTopics_;
    }

    public int getNumberOfIterations(){
        return numberOfIterations_;
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

        double[][] probabilities = new double[instances_.size()][numberOfTopics_];
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

        int numberOfDocuments = instances_.size();
        int[] documentTopicArray = new int[numberOfDocuments];

        for(int i = 0;i < numberOfDocuments;i++){
            documentTopicArray[i] = Utilities.indexOfMax(documentTopicProbabilities[i]);
        }

        return documentTopicArray;
    }

    public void start() throws IOException {
        hasRun_ = false;

        if(lda_ == null) {
            lda_ = new ParallelTopicModel(numberOfTopics_);
        }
        else{
            lda_.setNumTopics(numberOfTopics_);
        }
        lda_.setNumIterations(numberOfIterations_);
        lda_.setNumThreads(numberOfThreads_);
        lda_.addInstances(instances_);
        lda_.estimate();

        hasRun_ = true;
    }

    public LDA setDocuments(List<Document> documents){
        documents_ = documents;

        createInstances(documents_);

        return this;
    }

    public LDA setNumberOfTopics(int numberOfTopics){
        numberOfTopics_ = numberOfTopics;

        return this;
    }

    public LDA setNumberOfIterations(int numberOfIterations){
        numberOfIterations_ = numberOfIterations;

        return this;
    }

    public LDA setNumberOfThreads(int numberOfThreads){
        numberOfThreads_ = numberOfThreads;

        return this;
    }

    public boolean hasRun(){
        return hasRun_;
    }

    private List<Document> documents_;

    private InstanceList instances_;

    private int numberOfTopics_;
    private int numberOfIterations_;

    private int numberOfThreads_;

    private ParallelTopicModel lda_;

    private volatile boolean hasRun_ = false;

}
