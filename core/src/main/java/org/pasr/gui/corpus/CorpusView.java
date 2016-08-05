package org.pasr.gui.corpus;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import org.pasr.database.DataBase;
import org.pasr.database.corpus.Index;

import java.io.IOException;
import java.util.Optional;

import static org.pasr.utilities.Utilities.getResource;


public class CorpusView extends SplitPane {
    public CorpusView () throws IOException {
        FXMLLoader loader = new FXMLLoader(getResource("/fxml/corpus/view.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        loader.load();
    }

    @FXML
    public void initialize(){
        entryListView.getItems().addAll(DataBase.getInstance().getCorpusEntryList());
        entryListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if(newValue == null){
                    textArea.setText("");
                }
                else{
                    textArea.setText(
                        DataBase.getInstance().getCorpusByID(newValue.getId()).getText()
                    );
                }
        });
    }

    public void selectCorpus(int corpusID){
        Optional<Index.Entry> selectedEntry = entryListView.getItems().stream()
            .filter(entry -> entry.getId() == corpusID)
            .findFirst();

        if(selectedEntry.isPresent()){
            entryListView.getSelectionModel().select(selectedEntry.get());
        }
    }

    @FXML
    private ListView<Index.Entry> entryListView;

    @FXML
    private TextArea textArea;

}
