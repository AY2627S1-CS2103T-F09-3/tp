package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PreorderBuilder;

public class MainWindowTest {
    // The smallest window content: both columns at their minimum widths, with the gaps between and around them, and
    // the window's minimum height of 420 px less the title bar
    private static final double MIN_CONTENT_WIDTH = 780;
    private static final double MIN_CONTENT_HEIGHT = 380;
    // The content of a window at the default height of 600 px
    private static final double DEFAULT_CONTENT_HEIGHT = 560;
    private static final double CUSTOMER_LIST_MIN_HEIGHT = 80;
    private static final double MAIN_COLUMN_MIN_WIDTH = 450;
    private static final double SIDE_PANEL_MIN_WIDTH = 300;
    private static final double SIDE_PANEL_MAX_WIDTH = 460;

    @TempDir
    public Path temporaryFolder;

    private final Person delivery = new PersonBuilder().withName("Alice Tan").withPostalCode("012345")
            .withPreorder(new PreorderBuilder().withDate("2026-12-20").withMethod(FulfilmentMethod.DELIVERY)
                    .withHandoverNotes("Call on arrival")
                    .withItems(new ItemBuilder().withName("Pineapple Tarts").build()).build()).build();
    private final Person collection = new PersonBuilder().withName("Bob Tan").withPostalCode("530123")
            .withPreorder(new PreorderBuilder().withMethod(FulfilmentMethod.COLLECTION)
                    .withItems(new ItemBuilder().build()).build()).build();

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another UI test has already started JavaFX.
        }
    }

    @Test
    public void sidePanel_deliverySummaryFollowsDisplayedSearchResults() throws Exception {
        runInWindow((window, logic, root) -> {
            Parent summary = (Parent) root.lookup(".delivery-summary");
            assertShownWithinWindow(root, summary);
            ListView<?> customers = (ListView<?>) window.getPersonListPanel().getRoot().lookup("#personListView");
            assertEquals(List.of(delivery, collection), customers.getItems());
            assertEquals("1 outstanding customer stop(s) | 1 postal group(s)", totals(summary));
            assertTrue(text(summary).contains("Alice Tan | " + delivery.getPhone().value));
            assertTrue(text(summary).contains("Delivery | 20 Dec 2026\nHandover notes: Call on arrival"));

            logic.execute("find m/collection");
            assertEquals(List.of(collection), customers.getItems());
            assertEquals("0 outstanding customer stop(s) | 0 postal group(s)", totals(summary));
            assertEquals("No outstanding deliveries in the current results.", text(summary));
            assertShownWithinWindow(root, summary);

            logic.execute("find n/Nobody");
            assertEquals(List.of(), customers.getItems());
            assertEquals("No outstanding deliveries in the current results.", text(summary));
            assertShownWithinWindow(root, summary);

            logic.execute("list");
            assertEquals(List.of(delivery, collection), customers.getItems());
            assertEquals("1 outstanding customer stop(s) | 1 postal group(s)", totals(summary));
            assertShownWithinWindow(root, summary);
        });
    }

    @Test
    public void sidePanel_productSearch_showsProductSummaryInsteadOfDeliverySummary() throws Exception {
        runInWindow((window, logic, root) -> {
            Node deliverySummary = root.lookup(".delivery-summary");
            Node productSummary = root.lookup(".product-summary");

            logic.execute("find d/Pineapple Tarts");
            layOut(root, MIN_CONTENT_WIDTH, MIN_CONTENT_HEIGHT);
            Label productHeading = (Label) root.lookup("#productHeading");
            assertEquals("Preparation: Pineapple Tarts | All statuses", productHeading.getText());
            assertShownWithinWindow(root, productSummary);
            assertFalse(isShown(deliverySummary));

            logic.execute("list");
            layOut(root, MIN_CONTENT_WIDTH, MIN_CONTENT_HEIGHT);
            assertShownWithinWindow(root, deliverySummary);
            assertFalse(isShown(productSummary));
        });
    }

    @Test
    public void sidePanel_helpCommand_showsHelpUntilNextCommand() throws Exception {
        runInWindow((window, logic, root) -> {
            Node help = root.lookup(".help-panel");
            Node deliverySummary = root.lookup(".delivery-summary");
            assertFalse(isShown(help));

            enterCommand(root, "help");
            assertShownWithinWindow(root, help);
            assertFalse(isShown(deliverySummary));

            // A command that fails leaves help in place, so it can be read while fixing the command
            enterCommand(root, "add");
            assertTrue(isShown(help));

            enterCommand(root, "list");
            assertFalse(isShown(help));
            assertShownWithinWindow(root, deliverySummary);

            window.handleHelp();
            assertShownWithinWindow(root, help);
        });
    }

    @Test
    public void columns_minimumWindow_keepMinimumWidths() throws Exception {
        runInWindow((window, logic, root) -> {
            layOut(root, MIN_CONTENT_WIDTH, MIN_CONTENT_HEIGHT);
            assertTrue(width(root, "#mainColumn") >= MAIN_COLUMN_MIN_WIDTH);
            assertTrue(width(root, "#sidePanelPlaceholder") >= SIDE_PANEL_MIN_WIDTH);

            // A wider window gives the extra width to the customer list, not the side panel
            layOut(root, 1400, 800);
            assertTrue(width(root, "#sidePanelPlaceholder") <= SIDE_PANEL_MAX_WIDTH);
            assertTrue(width(root, "#mainColumn") > 1400 - 2 * SIDE_PANEL_MAX_WIDTH);
        });
    }

    @Test
    public void layout_narrowWindow_tightensGapsAroundCustomerList() throws Exception {
        runInWindow((window, logic, root) -> {
            layOut(root, 1000, 700);
            double defaultLeftGap = cardTextLeft(root);
            assertFalse(isCompact(root));

            layOut(root, MIN_CONTENT_WIDTH, MIN_CONTENT_HEIGHT);
            assertTrue(isCompact(root));
            double compactLeftGap = cardTextLeft(root);
            assertTrue(compactLeftGap < defaultLeftGap - 20);

            // Still narrow, so the gaps stay compact
            layOut(root, 880, MIN_CONTENT_HEIGHT);
            assertTrue(isCompact(root));
            assertEquals(compactLeftGap, cardTextLeft(root), 0.5);

            layOut(root, 1000, 700);
            assertFalse(isCompact(root));
            assertEquals(defaultLeftGap, cardTextLeft(root), 0.5);
        });
    }

    @Test
    public void card_maximumLengthTagAtMinimumWidth_wrapsInsideCard() throws Exception {
        runInWindow((window, logic, root) -> {
            String longTag = "W".repeat(30);
            enterCommand(root, "tag 1 t/" + longTag);
            enterCommand(root, "tag 1 t/vip");
            Parent nameAndTags = (Parent) root.lookup("#nameAndTags");
            List<Label> tags = nameAndTags.getChildrenUnmodifiable().stream()
                    .filter(node -> node.getStyleClass().contains(PersonCard.TAG_STYLE_CLASS))
                    .map(Label.class::cast).toList();
            Label longChip = tags.stream().filter(tag -> tag.getText().equals(longTag)).findFirst().orElseThrow();
            Label nextChip = tags.get(tags.indexOf(longChip) + 1);
            Bounds line = nameAndTags.localToScene(nameAndTags.getLayoutBounds());
            Bounds longChipBounds = longChip.localToScene(longChip.getLayoutBounds());

            assertTrue(longChipBounds.getMaxX() <= line.getMaxX() + 0.5,
                    "tag ends at " + longChipBounds.getMaxX() + ", past the name line at " + line.getMaxX());
            // The wrapped chip stays above the phone number and email under the name
            assertTrue(longChipBounds.getMaxY() <= line.getMaxY() + 0.5);
            // The label draws its whole text, rather than ending it in "..."
            assertEquals(longTag, ((Text) longChip.lookup(".text")).getText());
            // The next tag starts its own line at the left edge, without the gap between tags in front of it
            assertEquals(longChipBounds.getMinX(), nextChip.localToScene(nextChip.getLayoutBounds()).getMinX(), 0.5);
        });
    }

    private static boolean isCompact(Parent root) {
        return root.getPseudoClassStates().contains(PseudoClass.getPseudoClass("compact"));
    }

    /**
     * Returns how far the first card's name is from the left edge of the window.
     */
    private static double cardTextLeft(Parent root) {
        Node name = root.lookup("#nameAndTags");
        return name.localToScene(name.getBoundsInLocal()).getMinX();
    }

    @Test
    public void commandBox_invalidCommandAtDefaultHeight_showsWholeUsageMessage() throws Exception {
        runInWindow((window, logic, root) -> {
            TextArea resultDisplay = (TextArea) root.lookup("#resultDisplay");

            // Each of these shows its usage message, which are among the longest messages LeBake shows
            for (String command : List.of("add", "additem", "edit", "edititem", "find", "preorder")) {
                enterCommand(root, command);
                layOut(root, MIN_CONTENT_WIDTH, DEFAULT_CONTENT_HEIGHT);

                assertTrue(resultDisplay.getText().contains("Example: " + command), command);
                assertTrue(ResultDisplayTest.fitsWithoutScrolling(resultDisplay),
                        "result box scrolls for " + command);
            }
        });
    }

    @Test
    public void layout_longMessageAtMinimumHeight_keepsRoomForCustomerList() throws Exception {
        runInWindow((window, logic, root) -> {
            // Search badges and a long usage message both take height above the list
            enterCommand(root, "find n/Alice d/Pineapple Tarts s/pending m/delivery");
            enterCommand(root, "edititem");
            layOut(root, MIN_CONTENT_WIDTH, MIN_CONTENT_HEIGHT);

            assertTrue(height(root, "#personListPanelPlaceholder") >= CUSTOMER_LIST_MIN_HEIGHT);
            // The message scrolls instead, and the status bar stays in the window
            assertFalse(ResultDisplayTest.fitsWithoutScrolling((TextArea) root.lookup("#resultDisplay")));
            assertShownWithinWindow(root, root.lookup("#statusbarPlaceholder"));
        });
    }

    @Test
    public void fitWindowLength_savedLengthLongerThanScreen_shortenedToScreen() {
        // The default height of 600 px on a 1280x720 screen at 150%, which leaves 432 px above the taskbar
        assertEquals(432, MainWindow.fitWindowLength(600, 420, 432));
        assertEquals(600, MainWindow.fitWindowLength(600, 420, 1032));
    }

    @Test
    public void fitWindowLength_savedLengthBelowMinimum_raisedToMinimum() {
        // A width saved before the side panel was added
        assertEquals(800, MainWindow.fitWindowLength(740, 800, 1920));
        assertEquals(420, MainWindow.fitWindowLength(600, 420, 400));
    }

    /**
     * Runs {@code test} on a main window laid out at its minimum size, with a delivery and a collection customer.
     */
    private void runInWindow(WindowTest test) throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(delivery);
            model.addPerson(collection);
            Path dataFilePath = temporaryFolder.resolve("addressBook.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFilePath),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
            Logic logic = new LogicManager(model, storage);
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, logic, dataFilePath);
                window.fillInnerParts();
                Parent root = stage.getScene().getRoot();
                layOut(root, MIN_CONTENT_WIDTH, MIN_CONTENT_HEIGHT);
                test.run(window, logic, root);
            } finally {
                stage.close();
            }
        });
    }

    /**
     * Types {@code command} into the command box and presses Enter.
     */
    private static void enterCommand(Parent root, String command) {
        TextField commandBox = (TextField) root.lookup("#commandTextField");
        commandBox.setText(command);
        commandBox.fireEvent(new ActionEvent());
        layOut(root, MIN_CONTENT_WIDTH, MIN_CONTENT_HEIGHT);
    }

    /**
     * Lays out the window at the given size without opening it, twice so that sizes that depend on width settle.
     */
    private static void layOut(Parent root, double width, double height) {
        for (int pass = 0; pass < 2; pass++) {
            root.resize(width, height);
            root.applyCss();
            root.layout();
        }
    }

    private static void assertShownWithinWindow(Parent root, Node node) {
        layOut(root, MIN_CONTENT_WIDTH, MIN_CONTENT_HEIGHT);
        assertTrue(isShown(node));
        Bounds bounds = node.localToScene(node.getBoundsInLocal());
        assertTrue(bounds.getWidth() > 0);
        assertTrue(bounds.getHeight() > 0);
        assertTrue(root.localToScene(root.getLayoutBounds()).contains(bounds));
    }

    /**
     * Returns true if {@code node} and all its parents are visible and take part in the layout.
     */
    private static boolean isShown(Node node) {
        for (Node current = node; current != null; current = current.getParent()) {
            if (!current.isVisible() || !current.isManaged()) {
                return false;
            }
        }
        return true;
    }

    private static double width(Parent root, String selector) {
        return ((Region) root.lookup(selector)).getWidth();
    }

    private static double height(Parent root, String selector) {
        return ((Region) root.lookup(selector)).getHeight();
    }

    private static String totals(Parent summary) {
        return ((Label) summary.lookup("#totals")).getText();
    }

    private static String text(Parent summary) {
        return ((VBox) summary.lookup("#stops")).getChildren().stream()
                .map(node -> ((Label) node).getText()).collect(Collectors.joining("\n"));
    }

    private static void runOnFxThread(Executable action) throws Exception {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.execute();
                result.complete(null);
            } catch (Throwable error) {
                result.completeExceptionally(error);
            }
        });
        result.get(10, TimeUnit.SECONDS);
    }

    /**
     * A check run on a laid-out main window.
     */
    @FunctionalInterface
    private interface WindowTest {
        void run(MainWindow window, Logic logic, Parent root) throws Exception;
    }
}
