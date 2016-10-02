package org.pasr.gui.nodes;

import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;


/**
 * @class IntegerField
 * @brief Implementation of a TextField that accepts only integer values
 */
public class IntegerField extends TextField {

    /**
     * @brief Default Constructor
     */
    public IntegerField () {
        super();

        updateFilter();
    }

    /**
     * @brief Sets the minimum value for this field
     *
     * @param minValue
     *     The new minimum value
     */
    public void setMinValue (int minValue) {
        minValue_ = minValue;
    }

    /**
     * @brief Sets the maximum value for this field
     *
     * @param maxValue
     *     The new maximum value
     */
    public void setMaxValue (int maxValue) {
        maxValue_ = maxValue;
    }

    /**
     * @brief Sets the TextFormatter for this field
     */
    public void updateFilter () {
        setTextFormatter(new TextFormatter<Integer>(change -> {
            String resultingText = change.getControlNewText();

            if (resultingText.isEmpty()) {
                change.setText(String.valueOf(minValue_));
                return change;
            }

            int newValue;
            // Make sure that, if the change is accepted, the resulting value will be a parsable
            // integer
            try {
                newValue = Integer.parseInt(resultingText);
            } catch (Exception e) {
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

    /**
     * @brief Returns the value of this field
     *
     * @return The value of this field
     */
    public int getValue () {
        return Integer.parseInt(getText());
    }

    private int minValue_ = Integer.MIN_VALUE; //!< The minimum value for this field
    private int maxValue_ = Integer.MAX_VALUE; //!< The maximum value for this field

}
