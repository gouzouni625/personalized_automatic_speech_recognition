package org.pasr.utilities.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;


/**
 * @class PrettyFormatter
 * @brief Log Formatter
 */
public class PrettyFormatter extends SimpleFormatter {

    /**
     * @brief Returns the give LogRecord message formatted
     *
     * @param record
     *     The LogRecord the message of which to format
     *
     * @return The formatted LogRecord message
     */
    @Override
    public synchronized String format (LogRecord record) {
        Level level = record.getLevel();
        String message = record.getMessage();

        if (level == Level.SEVERE || level == Level.FINEST) {
            message = message.replaceAll("\n", "\n        ");
        }
        else if (level == Level.WARNING) {
            message = message.replaceAll("\n", "\n         ");
        }
        else if (level == Level.INFO || level == Level.FINE) {
            message = message.replaceAll("\n", "\n      ");
        }
        else if (level == Level.FINER) {
            message = message.replaceAll("\n", "\n       ");
        }

        record.setMessage(message);
        return super.format(record);
    }

}
