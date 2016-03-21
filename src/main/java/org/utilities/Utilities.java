package org.utilities;

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

}
