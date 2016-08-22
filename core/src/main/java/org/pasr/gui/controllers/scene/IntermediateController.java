package org.pasr.gui.controllers.scene;


import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;


public class IntermediateController extends Controller{
    public IntermediateController(Controller.API api){
        super(api);
    }

    @FXML
    public void initialize(){
        label.setText(((API) api_).getMessage());
    }

    public interface API extends Controller.API{
        String getMessage();
    }

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Label label;

}
