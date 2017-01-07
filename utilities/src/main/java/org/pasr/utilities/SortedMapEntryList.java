package org.pasr.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @class SortedMapEntryList
 * @brief Implements a List of Map.Entry object that will sort itself
 *
 * @param <K>
 *     The key type of the Map.Entry
 * @param <V>
 *     The value type of the Map.Entry
 */
public class SortedMapEntryList<K, V extends Comparable<V>> extends ArrayList<Map.Entry<K, V>> {

    /**
     * @brief Constructor
     *
     * @param size
     *     The size of this List
     * @param ascending
     *     Whether the sorting should be ascending
     */
    public SortedMapEntryList (int size, boolean ascending) {
        super(size);

        size_ = size;
        ascending_ = ascending;
    }

    /**
     * @brief Adds a Map.Entry to this List
     *
     * @param entry
     *     The Map.Entry to be added
     *
     * @return True if this List changed during the adding
     */
    @Override
    public boolean add (Map.Entry<K, V> entry) {
        if (size() == 0) {
            super.add(entry);
            return true;
        }

        boolean added = addSorted(entry);
        if (! added) {
            if (size() < size_) {
                super.add(entry);
                added = true;
            }
        }

        if (size() > size_) {
            removeRange(size_, size());
            return true;
        }

        return added;
    }

    private boolean addSorted (Map.Entry<K, V> entry) {
        for (int i = 0, n = size(); i < n; i++) {
            if ((entry.getValue().compareTo(get(i).getValue()) > 0 && ! ascending_) ||
                (entry.getValue().compareTo(get(i).getValue()) < 0 && ascending_)) {
                super.add(i, entry);
                return true;
            }
        }

        return false;
    }

    /**
     * @brief Adds a Collection of Map.Entry objects
     *
     * @param c
     *     The Collection to add
     *
     * @return True if this List changed during the adding
     */
    @Override
    public boolean addAll (Collection<? extends Map.Entry<K, V>> c) {
        boolean changed = false;
        for (Map.Entry<K, V> entry : c) {
            changed |= add(entry);
        }

        return changed;
    }

    /**
     * @brief Returns a List of the keys of all the Map.Entry objects in this List
     *
     * @return A List of keys the Map.Entry objects in this List
     */
    public List<K> keyList () {
        return stream()
            .map(Map.Entry:: getKey)
            .collect(Collectors.toList());
    }

    /**
     * @brief Returns a List with of values of all the Map.Entry objects in this List
     *
     * @return A List of the values of all the Map.Entry objects in this List
     */
    public List<V> valueList () {
        return stream()
            .map(Map.Entry:: getValue)
            .collect(Collectors.toList());
    }

    private final int size_; //!< The size of this List
    private final boolean ascending_; //!< Flag denoting whether the sorting should be ascending

}
