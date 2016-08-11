package org.pasr.gui.corpus;


import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import org.pasr.database.DataBase;
import org.pasr.database.corpus.Index;
import org.pasr.gui.console.Console;

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
        fillEntryListView();
        entryListView.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if(newValue == null){
                    textArea.clear();
                }
                else{

                    try {
                        textArea.setText(DataBase.getInstance()
                            .getCorpusByID(newValue.getId()).getPrettyText());
                    } catch (IOException e) {
                        Console.getInstance().postMessage("Could not load corpus with id: " +
                            newValue + ".");

                        textArea.clear();
                        // Run later to avoid modified the list view from inside the listener
                        Platform.runLater(this :: fillEntryListView);
                    } catch (IllegalArgumentException e) {
                        Console.getInstance().postMessage("The selected corpus does not exist.");

                        textArea.clear();
                        // Run later to avoid modified the list view from inside the listener
                        Platform.runLater(this :: fillEntryListView);
                    }
                }
        });
    }

    private void fillEntryListView(){
        entryListView.setItems(
            FXCollections.observableArrayList(
                DataBase.getInstance().getCorpusEntryList()
            )
        );
    }

    public int getSelectedCorpusID(){
        Index.Entry selectedEntry = entryListView.getSelectionModel().getSelectedItem();

        if(selectedEntry == null){
            return -1;
        }
        else{
            return selectedEntry.getId();
        }
    }

    public void selectCorpus(int corpusID){
        Optional<Index.Entry> selectedEntry = entryListView.getItems().stream()
            .filter(entry -> entry.getId() == corpusID)
            .findFirst();

        if(selectedEntry.isPresent()){
            entryListView.getSelectionModel().select(selectedEntry.get());
        }
    }

    public void addSelectionListener(ChangeListener<Index.Entry> listener){
        if(listener != null) {
            entryListView.getSelectionModel().selectedItemProperty().addListener(listener);
        }
    }

    @FXML
    private ListView<Index.Entry> entryListView;

    @FXML
    private TextArea textArea;

}
