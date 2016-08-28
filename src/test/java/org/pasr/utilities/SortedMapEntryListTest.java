package org.pasr.utilities;


import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SortedMapEntryListTest {
    @Test
    public void testSortedMapEntryList(){
        Random random = new Random(System.currentTimeMillis());

        Map<Integer, Double> map = new HashMap<>();

        for(int i = 0;i < 10000;i++){
            map.put(random.nextInt(), random.nextDouble());
        }

        SortedMapEntryList<Integer, Double> list = new SortedMapEntryList<>(10000, true);
        list.addAll(map.entrySet());

        for(int i = 1;i < 10000;i++){
            assertTrue(list.get(i).getValue() >= list.get(i - 1).getValue());
        }

        list = new SortedMapEntryList<>(10000, false);
        list.addAll(map.entrySet());

        for(int i = 1;i < 10000;i++){
            assertTrue(list.get(i).getValue() <= list.get(i - 1).getValue());
        }

        list = new SortedMapEntryList<>(5000, true);
        list.addAll(map.entrySet());

        assertEquals(5000, list.size());
        for(int i = 1;i < 5000;i++){
            assertTrue(list.get(i).getValue() >= list.get(i - 1).getValue());
        }

        list = new SortedMapEntryList<>(5000, false);
        list.addAll(map.entrySet());

        assertEquals(5000, list.size());
        for(int i = 1;i < 5000;i++){
            assertTrue(list.get(i).getValue() <= list.get(i - 1).getValue());
        }
    }

}
