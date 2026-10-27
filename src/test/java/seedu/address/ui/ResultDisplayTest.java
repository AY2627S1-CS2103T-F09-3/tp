package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddItemCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteItemCommand;
import seedu.address.logic.commands.DeleteOrderCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditItemCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.PreorderCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.TagCommand;
import seedu.address.logic.commands.TrackCommand;
import seedu.address.logic.commands.UntagCommand;

/**
 * Checks that the result box shows every usage message in full, without scrolling.
 * Starting JavaFX needs a display; on Linux CI runners, one is provided by xvfb (see gradle.yml).
 */
public class ResultDisplayTest {

    // The result box's width in a window at its minimum width of 450 px, after the 10 px padding on each side
    private static final double MIN_WINDOW_RESULT_WIDTH = 430;

    private static final List<String> USAGE_MESSAGES = List.of(AddCommand.MESSAGE_USAGE,
            AddItemCommand.MESSAGE_USAGE, DeleteCommand.MESSAGE_USAGE, DeleteItemCommand.MESSAGE_USAGE,
            DeleteOrderCommand.MESSAGE_USAGE, EditCommand.MESSAGE_USAGE, EditItemCommand.MESSAGE_USAGE,
            FindCommand.MESSAGE_USAGE, HelpCommand.MESSAGE_USAGE, PreorderCommand.MESSAGE_USAGE,
            SortCommand.MESSAGE_USAGE, TagCommand.MESSAGE_USAGE, TrackCommand.MESSAGE_USAGE,
            UntagCommand.MESSAGE_USAGE);

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX was already started by an earlier test class
        }
    }

    @Test
    public void setFeedbackToUser_usageMessageAtMinimumWidth_showsWholeMessageWithoutScrolling() throws Exception {
        for (String usage : USAGE_MESSAGES) {
            String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage);
            Boolean fits = onFxThread(() -> fitsWithoutScrolling(showInScene(message, MIN_WINDOW_RESULT_WIDTH)));

            assertTrue(fits, "message does not fit: " + usage);
        }
    }

    @Test
    public void fitsWithoutScrolling_tooFewRows_false() throws Exception {
        String message = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        Boolean fits = onFxThread(() -> {
            TextArea textArea = showInScene(message, MIN_WINDOW_RESULT_WIDTH);
            textArea.setPrefRowCount(2);
            Region root = (Region) textArea.getParent();
            root.resize(MIN_WINDOW_RESULT_WIDTH, root.prefHeight(MIN_WINDOW_RESULT_WIDTH));
            root.layout();
            return fitsWithoutScrolling(textArea);
        });

        assertFalse(fits);
    }

    /**
     * Returns true if the whole text of {@code textArea} fits in its viewport, so it can be read without scrolling.
     * This is checked against the text's size, as the scroll bars are not laid out when the window is not shown.
     */
    public static boolean fitsWithoutScrolling(TextArea textArea) {
        ScrollPane scrollPane = (ScrollPane) textArea.lookup(".scroll-pane");
        Bounds text = textArea.lookup(".text").getLayoutBounds();
        Bounds viewport = scrollPane.getViewportBounds();
        return text.getHeight() <= viewport.getHeight() + 0.5 && text.getWidth() <= viewport.getWidth() + 0.5;
    }

    @Test
    public void constructor_beforeLayout_hasRoomForShortMessageOnly() throws Exception {
        Integer rows = onFxThread(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();
            resultDisplay.setFeedbackToUser("Listed all persons.");
            return ((TextArea) resultDisplay.getRoot().lookup("#resultDisplay")).getPrefRowCount();
        });

        assertEquals(3, rows);
    }

    @Test
    public void setFeedbackToUser_longerMessage_growsBox() throws Exception {
        double[] heights = onFxThread(() -> {
            ResultDisplay resultDisplay = new ResultDisplay();
            Region root = layOut(resultDisplay, MIN_WINDOW_RESULT_WIDTH);
            resultDisplay.setFeedbackToUser("Listed all persons.");
            root.layout();
            double shortHeight = root.prefHeight(MIN_WINDOW_RESULT_WIDTH);
            resultDisplay.setFeedbackToUser(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            root.layout();
            return new double[] {shortHeight, root.prefHeight(MIN_WINDOW_RESULT_WIDTH)};
        });

        assertTrue(heights[1] > heights[0]);
    }

    /**
     * Shows {@code message} in a result box of the given width, and returns the box's text area.
     */
    private static TextArea showInScene(String message, double width) {
        ResultDisplay resultDisplay = new ResultDisplay();
        Region root = layOut(resultDisplay, width);
        resultDisplay.setFeedbackToUser(message);
        // As in the window, the box gets the height it asks for
        root.resize(width, root.prefHeight(width));
        root.layout();
        root.resize(width, root.prefHeight(width));
        root.layout();
        return (TextArea) root.lookup("#resultDisplay");
    }

    private static Region layOut(ResultDisplay resultDisplay, double width) {
        Region root = resultDisplay.getRoot();
        new Scene(root).getStylesheets().add(ResultDisplay.class.getResource("/view/DarkTheme.css").toExternalForm());
        root.applyCss();
        root.resize(width, root.prefHeight(width));
        root.layout();
        return root;
    }

    private static <T> T onFxThread(Callable<T> task) throws Exception {
        CompletableFuture<T> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                result.complete(task.call());
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result.get(10, TimeUnit.SECONDS);
    }
}
