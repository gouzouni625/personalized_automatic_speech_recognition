package org.utilities;

public class Margin {
    public Margin(int leftIndex, int rightIndex){
        leftIndex_ = leftIndex;
        rightIndex_ = rightIndex;

        length_ = rightIndex - leftIndex;
    }

    public final int leftIndex_;
    public final int rightIndex_;
    public final int length_;

}
