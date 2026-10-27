package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;

/**
 * A UI component that displays the result of a command execution.
 * The box wraps long lines and grows to show the whole message. Only in a window too short for both the message and
 * the customer list's minimum height does the message scroll.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";

    // Short results fit in this many rows, so the list below does not jump after every command
    private static final int MIN_ROWS = 3;
    // A little more than the text area's padding and border, so the lines are counted at a width no wider than theirs
    private static final double TEXT_INSETS = 30;

    @FXML
    private TextArea resultDisplay;

    /**
     * Creates an empty result box, which fits its rows to the message again whenever its width changes.
     */
    public ResultDisplay() {
        super(FXML);
        // Starts at the size of a short message, as the text area's default of 10 rows would only shrink to fit
        // after the window's first layout
        resultDisplay.setPrefRowCount(MIN_ROWS);
        resultDisplay.widthProperty().addListener((observable, oldWidth, newWidth) -> fitRowsToText());
    }

    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        resultDisplay.setText(feedbackToUser);
        fitRowsToText();
    }

    /**
     * Sets the number of rows to the number of lines the message wraps into at the box's current width.
     */
    private void fitRowsToText() {
        double wrappingWidth = resultDisplay.getWidth() - TEXT_INSETS;
        if (wrappingWidth <= 0) {
            return; // not laid out yet; the width listener calls this again once it is
        }
        Text message = new Text(resultDisplay.getText());
        message.setFont(resultDisplay.getFont());
        message.setWrappingWidth(wrappingWidth);
        Text oneLine = new Text("X");
        oneLine.setFont(resultDisplay.getFont());

        double lineCount = message.getLayoutBounds().getHeight() / oneLine.getLayoutBounds().getHeight();
        resultDisplay.setPrefRowCount(Math.max(MIN_ROWS, (int) Math.round(lineCount)));
    }
}
