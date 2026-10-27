package seedu.address.ui;

import java.util.List;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.commons.core.LogsCenter;

/**
 * Shows the format of every command and the link to the User Guide, in the side panel.
 */
public class HelpPanel extends UiPart<Region> {

    public static final String USERGUIDE_URL = "https://ay2627s1-cs2103t-f09-3.github.io/tp/UserGuide.html";

    /** The format of every command, in the order of the User Guide's command summary. */
    public static final List<String> COMMAND_FORMATS = List.of(
            "add n/NAME p/PHONE e/EMAIL a/ADDRESS c/POSTAL_CODE",
            "edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [c/POSTAL_CODE]",
            "delete INDEX",
            "tag INDEX t/TAG",
            "untag INDEX t/TAG",
            "preorder INDEX [f/DATE] [m/METHOD] [h/HANDOVER_NOTES]",
            "additem INDEX d/NAME q/QUANTITY s/STATUS [r/REMARK]",
            "edititem INDEX i/ITEM_INDEX [d/NAME] [q/QUANTITY] [s/STATUS] [r/REMARK]",
            "deleteitem INDEX i/ITEM_INDEX",
            "deleteorder INDEX",
            "find [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [c/POSTAL_CODE_PREFIX] [t/TAG] [d/PRODUCT] "
                    + "[s/STATUS] [f/DATE] [m/METHOD] [h/HANDOVER_NOTES]",
            "track [s/STATUS]",
            "list",
            "sort date",
            "group postal",
            "clear",
            "help",
            "exit");

    private static final Logger logger = LogsCenter.getLogger(HelpPanel.class);
    private static final String FXML = "HelpPanel.fxml";
    private static final String COMMAND_STYLE_CLASS = "help-command";

    @FXML
    private VBox commands;

    @FXML
    private Label userGuideUrl;

    /**
     * Creates a help panel listing every command.
     */
    public HelpPanel() {
        super(FXML);
        for (String format : COMMAND_FORMATS) {
            Label command = new Label(format);
            command.getStyleClass().add(COMMAND_STYLE_CLASS);
            command.setWrapText(true);
            commands.getChildren().add(command);
        }
        userGuideUrl.setText(USERGUIDE_URL);
    }

    /**
     * Copies the URL of the User Guide to the clipboard.
     */
    @FXML
    private void copyUrl() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent url = new ClipboardContent();
        url.putString(USERGUIDE_URL);
        clipboard.setContent(url);
        logger.fine("Copied the User Guide URL");
    }
}
