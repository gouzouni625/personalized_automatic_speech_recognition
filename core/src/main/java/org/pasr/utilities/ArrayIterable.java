package org.pasr.utilities;

import java.util.Iterator;

public class ArrayIterable<T> implements Iterable<T> {
    public ArrayIterable(T[] array){
        array_ = array;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {
            public boolean hasNext(){
                return index_ < array_.length;
            }

            public T next(){
                T object = array_[index_];

                index_++;

                return object;
            }

            private int index_ = 0;
        };
    }

    private final T[] array_;

}
