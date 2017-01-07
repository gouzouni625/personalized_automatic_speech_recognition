package org.pasr.model.asr.language;

import org.pasr.model.text.WordSequence;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Scanner;

import static org.apache.commons.lang3.math.NumberUtils.isNumber;


/**
 * @class LanguageModel
 * @brief Implements of a 3-gram language model in the ARPA-standard format
 *
 * @see <a href="http://cmusphinx.sourceforge.net/wiki/tutoriallm">http://cmusphinx.sourceforge.net/wiki/tutoriallm</a>
 */
public class LanguageModel {

    /**
     * @brief Constructor
     *        Made private to prevent direct instantiation. To create a LanguageModel
     *        createFromInputStream method should be used
     *
     * @param probabilities
     *     The probabilities map
     * @param backOffWeights
     *     The back-off weights map
     */
    private LanguageModel (Hashtable<String, Double> probabilities,
                           Hashtable<String, Double> backOffWeights) {
        probabilities_ = probabilities;
        backOffWeights_ = backOffWeights;
    }

    /**
     * @brief Creates a Language Model from an InputStream
     *
     * @param inputStream
     *     The InputStream to read from
     *
     * @return The created Language Model
     */
    public static LanguageModel createFromInputStream (InputStream inputStream) {
        Hashtable<String, Double> probabilities = new Hashtable<>();
        Hashtable<String, Double> backOffWeights = new Hashtable<>();

        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine()) {
            if (scanner.nextLine().equals("\\data\\")) {
                break;
            }
        }

        boolean save = false;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.equals("\\1-grams:")) {
                save = true;
                continue;
            }

            if (line.equals("") || line.equals("\\2-grams:") || line.equals("\\3-grams:")) {
                continue;
            }

            if (line.equals("\\end\\")) {
                save = false;
            }

            if (save) {
                String[] tokens = line.replaceAll("\\t", " ").split(" ");

                // 1-gram
                if (tokens.length == 3) {
                    probabilities.put(tokens[1], Math.pow(10, Double.parseDouble(tokens[0])));
                    backOffWeights.put(tokens[1], Math.pow(10, Double.parseDouble(tokens[2])));
                }
                else {
                    // 2-gram
                    if (isNumber(tokens[tokens.length - 1])) {
                        String key = String.join(" ", tokens[1], tokens[2]);

                        probabilities.put(key, Math.pow(10, Double.parseDouble(tokens[0])));
                        backOffWeights.put(key, Math.pow(10, Double.parseDouble(tokens[3])));
                    }
                    else { // 3-gram
                        probabilities.put(String.join(" ", tokens[1], tokens[2], tokens[3]),
                            Math.pow(10, Double.parseDouble(tokens[0])));
                    }
                }
            }
        }
        scanner.close();

        return new LanguageModel(probabilities, backOffWeights);
    }

    /**
     * @brief Returns the probability of the given WordSequence
     *
     * @param wordSequence
     *     The WordSequence
     *
     * @return The probability of the given WordSequence
     */
    public double getProbability (WordSequence wordSequence) {
        if (wordSequence == null) {
            return 0;
        }

        String[] words = wordSequence.getWordTextList().stream()
            .toArray(String[] ::new);

        int numberOfWords = words.length;

        if (numberOfWords == 0) {
            return 0;
        }
        else if (numberOfWords == 1) {
            return p1(words);
        }
        else if (numberOfWords == 2) {
            return p2(words);
        }
        else if (numberOfWords == 3) {
            return p3(words);
        }
        else {
            double probability = p1(words[0]) * p2(words[0], words[1]);

            for (int i = 2; i < numberOfWords; i++) {
                probability *= p3(words[i - 2], words[i - 1], words[i]);
            }

            return probability;
        }
    }

    /**
     * @brief Returns the 1-gram probability of the given word sequence
     *
     * @param words
     *     The sequence of words
     *
     * @return The 1-gram probability of the given word sequence
     */
    private double p1 (String... words) {
        // Search for the 1-gram probability.
        Double probability = probabilities_.get(words[0]);

        // If the 1-gram probability doesn't exist, return 0.
        return (probability == null ? 0 : probability);
    }

    /**
     * Returns the 2-gram probability of the given word sequence
     *
     * @param words
     *     The sequence of words
     *
     * @return The 2-gram probability of the given word sequence
     */
    private double p2 (String... words) {
        // Search for the 2-gram probability.
        Double probability = probabilities_.get(String.join(" ", (CharSequence[]) words));

        // If the 2-gram doesn't exist, use the back-off weight according to the formula:
        // p(wd2|wd1) = bo_wt_1(wd1)*p_1(wd2)
        if (probability == null) {
            Double backOffWeight = backOffWeights_.get(words[0]);
            probability = probabilities_.get(words[1]);

            if (backOffWeight == null || probability == null) {
                return 0;
            }
            else {
                return backOffWeight * probability;
            }
        }
        else {
            return probability;
        }
    }

    /**
     * Returns the 3-gram probability of the given word sequence
     *
     * @param words
     *     The sequence of words
     *
     * @return The 3-gram probability of the given word sequence
     */
    private double p3 (String... words) {
        // Search for the 3-gram probability.
        Double probability = probabilities_.get(String.join(" ", (CharSequence[]) words));

        // If the 3-gram probability doesn't exist, use the back-off weight according to the
        // formula:
        // p(wd3|wd1,wd2) = bo_wt_2(w1,w2)*p(wd3|wd2)
        if (probability == null) {
            Double backOffWeight = backOffWeights_.get(String.join(" ", words[0], words[1]));
            probability = p2(words[1], words[2]);

            if (backOffWeight == null) {
                return probability;
            }
            else {
                return backOffWeight * probability;
            }
        }
        else {
            return probability;
        }
    }

    private Hashtable<String, Double> probabilities_ = new Hashtable<>(); //!< The probabilities of
                                                                          //!< this language model
    private Hashtable<String, Double> backOffWeights_ = new Hashtable<>(); //!< The back-off weights
                                                                           //!< of this language
                                                                           //!< model

}
