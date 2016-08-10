package org.pasr.gui.dialog;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.pasr.prep.lda.LDA;

import java.io.IOException;
import java.util.List;

// Return a Map with keys the corpus name and values the list of documents for each corpus
public class LDAInteractDialog extends Dialog<MultiValuedMap<String, List<Long>>> {
    public LDAInteractDialog(LDA lda) throws IOException {
        super(new ArrayListValuedHashMap<>());

        if(lda == null || !lda.hasRun()){
            throw new IllegalArgumentException("Null or not started LDA.");
        }


    }
}
