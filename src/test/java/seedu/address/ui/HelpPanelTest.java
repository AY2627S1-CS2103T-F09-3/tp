package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddItemCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteItemCommand;
import seedu.address.logic.commands.DeleteOrderCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditItemCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.GroupPostalCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.PreorderCommand;
import seedu.address.logic.commands.SortCommand;
import seedu.address.logic.commands.TagCommand;
import seedu.address.logic.commands.TrackCommand;
import seedu.address.logic.commands.UntagCommand;

/**
 * Checks that the help panel lists every command.
 * Starting JavaFX needs a display; on Linux CI runners, one is provided by xvfb (see gradle.yml).
 */
public class HelpPanelTest {

    // Every command word that AddressBookParser accepts
    private static final List<String> COMMAND_WORDS = List.of(AddCommand.COMMAND_WORD, AddItemCommand.COMMAND_WORD,
            ClearCommand.COMMAND_WORD, DeleteCommand.COMMAND_WORD, DeleteItemCommand.COMMAND_WORD,
            DeleteOrderCommand.COMMAND_WORD, EditCommand.COMMAND_WORD, EditItemCommand.COMMAND_WORD,
            ExitCommand.COMMAND_WORD, FindCommand.COMMAND_WORD, GroupPostalCommand.COMMAND_WORD,
            HelpCommand.COMMAND_WORD, ListCommand.COMMAND_WORD, PreorderCommand.COMMAND_WORD,
            SortCommand.COMMAND_WORD, TagCommand.COMMAND_WORD, TrackCommand.COMMAND_WORD,
            UntagCommand.COMMAND_WORD);

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX was already started by an earlier test class
        }
    }

    @Test
    public void commandFormats_everyCommand_listedOnce() {
        for (String word : COMMAND_WORDS) {
            long count = HelpPanel.COMMAND_FORMATS.stream()
                    .filter(format -> format.equals(word) || format.startsWith(word + " "))
                    .count();
            assertEquals(1, count, word);
        }
        assertEquals(COMMAND_WORDS.size(), HelpPanel.COMMAND_FORMATS.size());
    }

    @Test
    public void constructor_showsEveryFormatAndUserGuideLink() throws Exception {
        Region root = onFxThread(() -> {
            Region panel = new HelpPanel().getRoot();
            // The scroll pane adds its content to the scene graph only once it is styled in a scene
            new Scene(panel);
            panel.applyCss();
            return panel;
        });
        VBox commands = (VBox) root.lookup("#commands");

        assertEquals(HelpPanel.COMMAND_FORMATS, commands.getChildren().stream()
                .map(Label.class::cast).map(Label::getText).toList());
        assertTrue(commands.getChildren().stream().allMatch(label -> ((Label) label).isWrapText()));
        assertEquals(HelpPanel.USERGUIDE_URL, ((Label) root.lookup("#userGuideUrl")).getText());
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
