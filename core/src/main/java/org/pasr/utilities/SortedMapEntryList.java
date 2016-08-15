package org.pasr.utilities;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class SortedMapEntryList<K, V extends Comparable<V>> extends ArrayList<Map.Entry<K, V>> {
    public SortedMapEntryList (int size, boolean ascending){
        super(size);

        size_ = size;
        ascending_ = ascending;
    }

    @Override
    public boolean add(Map.Entry<K, V> entry){
        if(size() == 0){
            super.add(entry);
            return true;
        }

        boolean added = addSorted(entry);
        if(!added){
            if(size() < size_){
                super.add(entry);
                added = true;
            }
        }

        if(size() > size_){
            removeRange(size_, size());
            return true;
        }

        return added;
    }

    private boolean addSorted(Map.Entry<K, V> entry){
        for(int i = 0, n = size();i < n;i++){
            if((entry.getValue().compareTo(get(i).getValue()) > 0 && !ascending_) ||
                (entry.getValue().compareTo(get(i).getValue()) < 0 && ascending_)){
                super.add(i, entry);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean addAll(Collection<? extends Map.Entry<K, V>> c){
        boolean changed = false;
        for(Map.Entry<K, V> entry : c){
            changed |= add(entry);
        }

        return changed;
    }

    public List<K> valueList(){
        return this.stream()
            .map(Map.Entry :: getKey)
            .collect(Collectors.toList());
    }

    private final int size_;
    private final boolean ascending_;

}
