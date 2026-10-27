package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
    @TempDir
    public Path temporaryFolder;

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another UI test has already started JavaFX.
        }
    }

    @Test
    public void fillInnerParts_deliverySummaryRemainsVisibleAndFollowsDisplayedSearchResults() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Person delivery = new PersonBuilder().withName("Alice Tan").withPostalCode("012345")
                    .withPreorder(new PreorderBuilder().withDate("2026-12-20").withMethod(FulfilmentMethod.DELIVERY)
                            .withHandoverNotes("Call on arrival")
                            .withItems(new ItemBuilder().withName("Pineapple Tarts").build()).build()).build();
            Person collection = new PersonBuilder().withName("Bob Tan").withPostalCode("530123")
                    .withPreorder(new PreorderBuilder().withMethod(FulfilmentMethod.COLLECTION)
                            .withItems(new ItemBuilder().build()).build()).build();
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
                root.applyCss();
                root.layout();
                StackPane placeholder = (StackPane) root.lookup("#deliverySummaryPlaceholder");
                assertEquals(1, placeholder.getChildren().size());
                Parent summary = (Parent) placeholder.getChildren().get(0);
                assertSummaryVisible(root, placeholder, summary);
                ListView<?> customers = (ListView<?>) window.getPersonListPanel().getRoot().lookup("#personListView");
                assertEquals(List.of(delivery, collection), customers.getItems());
                assertEquals("1 outstanding customer stop(s) | 1 postal group(s)", totals(summary));
                assertTrue(text(summary).contains("Alice Tan | " + delivery.getPhone().value));
                assertTrue(text(summary).contains("Delivery | 20 Dec 2026\nHandover notes: Call on arrival"));

                logic.execute("find m/collection");
                assertEquals(List.of(collection), customers.getItems());
                assertEquals("0 outstanding customer stop(s) | 0 postal group(s)", totals(summary));
                assertEquals("No outstanding deliveries in the current results.", text(summary));
                assertSummaryVisible(root, placeholder, summary);

                logic.execute("find n/Nobody");
                assertEquals(List.of(), customers.getItems());
                assertEquals("0 outstanding customer stop(s) | 0 postal group(s)", totals(summary));
                assertEquals("No outstanding deliveries in the current results.", text(summary));
                assertSummaryVisible(root, placeholder, summary);

                logic.execute("list");
                assertEquals(List.of(delivery, collection), customers.getItems());
                assertEquals("1 outstanding customer stop(s) | 1 postal group(s)", totals(summary));
                assertTrue(text(summary).contains("Alice Tan | " + delivery.getPhone().value));
                assertSummaryVisible(root, placeholder, summary);

                logic.execute("find d/Pineapple Tarts");
                Label productHeading = (Label) root.lookup("#productHeading");
                assertTrue(productHeading.isVisible());
                assertEquals("Preparation: Pineapple Tarts | All statuses", productHeading.getText());
                StackPane customerPlaceholder = (StackPane) root.lookup("#personListPanelPlaceholder");
                VBox customerView = (VBox) customerPlaceholder.getChildren().get(0);
                assertEquals(2, customerView.getChildren().size());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void commandBox_invalidCommandAtMinimumSize_showsWholeUsageMessage() throws Exception {
        runOnFxThread(() -> {
            Path dataFilePath = temporaryFolder.resolve("addressBook.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataFilePath),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")));
            Stage stage = new Stage();
            try {
                MainWindow window = new MainWindow(stage, new LogicManager(new ModelManager(), storage),
                        dataFilePath);
                window.fillInnerParts();
                Parent root = stage.getScene().getRoot();
                TextField commandBox = (TextField) root.lookup("#commandTextField");
                TextArea resultDisplay = (TextArea) root.lookup("#resultDisplay");

                // Each of these shows its usage message, which are among the longest messages LeBake shows
                for (String command : List.of("add", "additem", "edit", "edititem", "find", "preorder")) {
                    layOutAtMinimumSize(root);
                    commandBox.setText(command);
                    commandBox.fireEvent(new ActionEvent());
                    layOutAtMinimumSize(root);
                    layOutAtMinimumSize(root);

                    assertTrue(resultDisplay.getText().contains("Example: " + command), command);
                    assertTrue(ResultDisplayTest.fitsWithoutScrolling(resultDisplay),
                            "result box scrolls for " + command);
                }
            } finally {
                stage.close();
            }
        });
    }

    private static void layOutAtMinimumSize(Parent root) {
        root.resize(450, 600);
        root.applyCss();
        root.layout();
    }

    private static void assertSummaryVisible(Parent root, StackPane placeholder, Parent summary) {
        // Exercise the layout at the minimum FXML window dimensions without opening a test window.
        root.resize(450, 600);
        root.applyCss();
        root.layout();
        assertEquals(List.of(summary), placeholder.getChildren());
        for (Node node = summary; node != null; node = node.getParent()) {
            assertTrue(node.isVisible());
            assertTrue(node.isManaged());
        }
        Bounds bounds = summary.localToScene(summary.getBoundsInLocal());
        assertTrue(bounds.getWidth() > 0);
        assertTrue(bounds.getHeight() > 0);
        assertTrue(root.localToScene(root.getLayoutBounds()).contains(bounds));
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
}
