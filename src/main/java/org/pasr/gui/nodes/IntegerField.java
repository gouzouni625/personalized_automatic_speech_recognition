package org.pasr.gui.nodes;


import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;


public class IntegerField extends TextField {
    public IntegerField () {
        super();

        updateFilter();
    }

    public void setMinValue (int minValue) {
        minValue_ = minValue;
    }

    public void setMaxValue (int maxValue) {
        maxValue_ = maxValue;
    }

    public void updateFilter () {
        setTextFormatter(new TextFormatter<Integer>(change -> {
            String resultingText = change.getControlNewText();

            if(resultingText.isEmpty()){
                change.setText(String.valueOf(minValue_));
                return change;
            }

            int newValue;
            // Make sure that, if the change is accepted, the resulting value will be a parsable
            // integer
            try{
                newValue = Integer.parseInt(resultingText);
            }
            catch (Exception e){
                return null;
            }

            // Make sure that, if the change is accepted, the resulting value will be inside the
            // defined margin
            if (newValue < minValue_ || newValue > maxValue_) {
                return null;
            }
            else {
                return change;
            }
        }));
    }

    public int getValue(){
        return Integer.parseInt(getText());
    }

    private int minValue_ = Integer.MIN_VALUE;
    private int maxValue_ = Integer.MAX_VALUE;

}
