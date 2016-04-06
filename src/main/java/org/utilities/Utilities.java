package org.utilities;

import java.util.ArrayList;
import java.util.Collection;


public class Utilities {
    public static void objectCollectionToPrimitiveArray(Collection<Integer> collection, int[] array) {
        int index = 0;
        for (Integer object : collection) {
            array[index] = object;

            index++;
        }
    }

    public static void objectCollectionToPrimitiveArray(Collection<Integer[]> collection, int[][] array) {
        int index = 0;
        for (Integer[] object : collection) {
            array[index] = objectArrayToPrimitiveArray(object);

            index++;
        }
    }

    public static int[] objectArrayToPrimitiveArray(Integer[] objectArray) {
        int size = objectArray.length;

        int[] primitiveArray = new int[size];

        for (int i = 0; i < size; i++) {
            primitiveArray[i] = objectArray[i];
        }

        return primitiveArray;
    }

    public static <T> void collectionToArray(Collection<T> collection, T[] array) {
        int index = 0;

        for (T object : collection) {
            array[index] = object;

            index++;
        }
    }

    public static Margin[] arrayMargins(boolean[] array, boolean trues){
        ArrayList<Margin> margins = new ArrayList<Margin>();

        int start = - 1;

        int arrayLength = array.length;
        for(int i = 0;i < arrayLength;i++){
            if(array[i] == trues){
                if(start == - 1){
                    start = i;
                }
            }
            else{
                if(start != - 1) {
                    margins.add(new Margin(start, i));

                    start = -1;
                }
            }
        }
        if(start != -1){
            margins.add(new Margin(start, arrayLength));
        }

        Margin[] marginsArray = new Margin[margins.size()];
        collectionToArray(margins, marginsArray);

        return marginsArray;
    }

    public static String getResourcePath(String resource){
        return Utilities.class.getResource(resource).getPath();
    }

}
