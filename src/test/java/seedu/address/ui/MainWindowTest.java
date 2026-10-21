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
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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
    public void fillInnerParts_deliverySummaryFollowsDisplayedSearchResults() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Person delivery = new PersonBuilder().withName("Alice Tan").withPostalCode("012345")
                    .withPreorder(new PreorderBuilder().withDate("2026-12-20").withMethod(FulfilmentMethod.DELIVERY)
                            .withHandoverNotes("Call on arrival").withItems(new ItemBuilder().build()).build()).build();
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
                ListView<?> customers = (ListView<?>) window.getPersonListPanel().getRoot().lookup("#personListView");
                assertEquals(List.of(delivery, collection), customers.getItems());
                assertEquals("1 outstanding customer stop(s) | 1 postal group(s)", totals(summary));
                assertTrue(text(summary).contains("Alice Tan | " + delivery.getPhone().value));
                assertTrue(text(summary).contains("Delivery | 20 Dec 2026\nHandover notes: Call on arrival"));

                logic.execute("find m/collection");
                assertEquals(List.of(collection), customers.getItems());
                assertEquals("0 outstanding customer stop(s) | 0 postal group(s)", totals(summary));
                assertEquals("No outstanding deliveries in the current results.", text(summary));

                logic.execute("list");
                assertEquals(List.of(delivery, collection), customers.getItems());
                assertEquals("1 outstanding customer stop(s) | 1 postal group(s)", totals(summary));
                assertTrue(text(summary).contains("Alice Tan | " + delivery.getPhone().value));
            } finally {
                stage.close();
            }
        });
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
