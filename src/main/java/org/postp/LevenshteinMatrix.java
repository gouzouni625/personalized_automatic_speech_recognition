package org.postp;

import static java.lang.Integer.min;


public class LevenshteinMatrix {
    public LevenshteinMatrix(String source, String destination){
        source_ = source;
        destination_ = destination;

        calculateMatrix();
    }

    private void calculateMatrix(){
        int sourceLength = source_.length();
        int destinationLength = destination_.length();

        // Initialize the Levenshtein matrix.
        matrix_ = new int[destinationLength + 1][sourceLength + 1];
        for (int i = 0; i <= destinationLength; i++) {
            for (int j = 0; j <= sourceLength; j++) {
                if (j == 0 && i == 0) {
                    matrix_[0][0] = 0;
                } else if (i == 0) {
                    matrix_[0][j] = j;
                } else if (j == 0) {
                    matrix_[i][0] = i;
                } else {
                    matrix_[i][j] = 0;
                }
            }
        }

        // Build the Levenshtein matrix.
        int substitutionCost;
        for (int j = 1; j <= sourceLength; j++) {
            for (int i = 1; i <= destinationLength; i++) {
                if (destination_.charAt(i - 1) == source_.charAt(j - 1)) {
                    substitutionCost = 0;
                } else {
                    substitutionCost = 1;
                }

                matrix_[i][j] = min(
                        matrix_[i - 1][j] + 1, min(matrix_[i][j - 1] + 1, matrix_[i - 1][j - 1] + substitutionCost)
                );
            }
        }

        distance_ = matrix_[destinationLength][sourceLength];

        // Find the path.
        path_ = new int[distance_][];

        int currentScore = distance_;

        int row = destinationLength;
        int column = sourceLength;

        int leftValue;
        int aboveValue;
        int diagonalValue;

        int minValue;
        int pathIndex = 0;
        while (currentScore > 0) {
            leftValue = matrix_[row][column - 1];
            aboveValue = matrix_[row - 1][column];
            diagonalValue = matrix_[row - 1][column - 1];

            minValue = min(leftValue, min(aboveValue, diagonalValue));
            if (currentScore != minValue) {
                // Note that in Levenshtein matrix, columns start counting from 1 not zero.
                path_[pathIndex] = new int[] {row - 1, column - 1};
                pathIndex++;
            }

            if (minValue == diagonalValue) {
                row--;
                column--;
            } else if (minValue == leftValue) {
                column--;
            } else {
                row--;
            }

            currentScore = minValue;
        }
    }

    public int[][] getMatrix(){
        return matrix_;
    }

    public int[][] getPath(){
        return path_;
    }

    public int getDistance(){
        return distance_;
    }

    private String source_;
    private String destination_;

    private int[][] matrix_;

    private int[][] path_;

    private int distance_;

}
