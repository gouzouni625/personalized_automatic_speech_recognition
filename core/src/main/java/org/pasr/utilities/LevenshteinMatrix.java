package org.pasr.utilities;

import static java.lang.Integer.min;


public class LevenshteinMatrix<T extends Comparable<T>> {
    public LevenshteinMatrix(T[] source, T[] destination){
        source_ = source;
        destination_ = destination;

        calculateMatrix();
    }

    private void calculateMatrix(){
        int sourceLength = source_.length;
        int destinationLength = destination_.length;

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
                if (destination_[i - 1].compareTo(source_[j - 1]) == 0) {
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

        int previousRow;
        int previousColumn;

        int leftValue;
        int aboveValue;
        int diagonalValue;

        int minValue;
        int pathIndex = 0;
        while (currentScore > 0) {
            if(row == 0 && column == 0){
                break;
            }

            previousRow = row > 0 ? row - 1 : 0;
            previousColumn = column > 0 ? column - 1 : 0;

            leftValue = matrix_[row][previousColumn];
            aboveValue = matrix_[previousRow][column];
            diagonalValue = matrix_[previousRow][previousColumn];

            minValue = min(leftValue, min(aboveValue, diagonalValue));
            if (currentScore != minValue) {
                // Note that in Levenshtein matrix, columns start counting from 1 not zero.
                path_[pathIndex] = new int[] {previousRow, previousColumn};
                pathIndex++;
            }

            if (minValue == diagonalValue && row != previousRow && column != previousColumn) {
                row--;
                column--;
            } else if (minValue == leftValue && column != previousColumn) {
                column--;
            } else {
                row--;
            }

            currentScore = minValue;
        }
    }

    @Override
    public String toString(){
        int sourceLength = source_.length;
        int destinationLength = destination_.length;

        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("|" + "  " + "|" + "  ");
        for(Comparable symbol : source_){
            stringBuilder.append(symbol.toString()).append("  ");
        }
        stringBuilder.append("\n");

        stringBuilder.append("|").append("  ");
        for(int i = 0;i <= destinationLength;i++){
            if(i > 0) {
                stringBuilder.append(destination_[i - 1].toString()).append("  ");
            }

            for(int j = 0;j <= sourceLength;j++){
                if(matrix_[i][j] >= 10) {
                    stringBuilder.append(matrix_[i][j]).append(" ");
                }
                else{
                    stringBuilder.append(matrix_[i][j]).append("  ");
                }
            }
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
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

    private T[] source_;
    private T[] destination_;

    private int[][] matrix_;

    private int[][] path_;

    private int distance_;

}
