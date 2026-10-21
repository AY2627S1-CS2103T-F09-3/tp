package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.GroupPostalCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.search.SearchCriteria;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PreorderBuilder;

public class DeliverySummaryPanelTest {
    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another UI test has already started JavaFX.
        }
    }

    @Test
    public void currentResultsAndCustomerChanges_refreshContactsAndTotals() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Person customer = new PersonBuilder().withName("Alice Tan").withPostalCode("012345")
                    .withPreorder(new PreorderBuilder().withDate("2026-12-20").withMethod(FulfilmentMethod.DELIVERY)
                            .withHandoverNotes("Call on arrival").withItems(new ItemBuilder().build()).build()).build();
            model.addPerson(customer);
            DeliverySummaryPanel panel = new DeliverySummaryPanel(model.getFilteredPersonList());
            new Scene(panel.getRoot(), 410, 200);
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            assertEquals("1 outstanding customer stop(s) | 1 postal group(s)", totals(panel));
            assertTrue(text(panel).contains("POSTAL PREFIX 01"));
            assertTrue(text(panel).contains(customer.getPhone().value));
            assertTrue(text(panel).contains(customer.getAddress().value));
            assertTrue(text(panel).contains("Delivery | 20 Dec 2026\nHandover notes: Call on arrival"));

            new GroupPostalCommand().execute(model);
            assertEquals("1 outstanding customer stop(s) | 1 postal group(s)", totals(panel));
            model.updateFilteredPersonList(new SearchCriteria(List.of(person -> false)));
            assertEquals("0 outstanding customer stop(s) | 0 postal group(s)", totals(panel));
            assertTrue(text(panel).contains("No outstanding deliveries"));
            model.updateFilteredPersonList(ModelManager.SEARCH_CRITERIA_SHOW_ALL_PERSONS);
            Person collection = new PersonBuilder(customer).withPreorder(new PreorderBuilder()
                    .withMethod(FulfilmentMethod.COLLECTION).withItems(new ItemBuilder().build()).build()).build();
            model.setPerson(customer, collection);
            assertEquals("0 outstanding customer stop(s) | 0 postal group(s)", totals(panel));
            model.setPerson(collection, new PersonBuilder(customer).withPhone("89999999").build());
            assertTrue(text(panel).contains("89999999"));
            assertEquals("1 outstanding customer stop(s) | 1 postal group(s)", totals(panel));
        });
    }

    @Test
    public void customersSharingPostalCode_shareOneHeaderAndKeepSortedContacts() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            Person first = new PersonBuilder().withName("Alice Tan").withPostalCode("012345")
                    .withPreorder(new PreorderBuilder().withMethod(FulfilmentMethod.DELIVERY)
                            .withItems(new ItemBuilder().build()).build()).build();
            Person sameGroup = new PersonBuilder().withName("Ben Tan").withPostalCode("012345")
                    .withPreorder(first.getPreorder().orElseThrow()).build();
            Person otherGroup = new PersonBuilder().withName("Charlie Tan").withPostalCode("530123")
                    .withPreorder(first.getPreorder().orElseThrow()).build();
            model.addPerson(otherGroup);
            model.addPerson(sameGroup);
            model.addPerson(first);
            DeliverySummaryPanel panel = new DeliverySummaryPanel(model.getFilteredPersonList());
            new Scene(panel.getRoot(), 410, 200);
            panel.getRoot().applyCss();
            panel.getRoot().layout();

            assertEquals("3 outstanding customer stop(s) | 2 postal group(s)", totals(panel));
            assertEquals(List.of("POSTAL PREFIX 01", "POSTAL PREFIX 53"), groupHeaders(panel));
            assertEquals(List.of("Alice Tan", "Ben Tan", "Charlie Tan"), contactNames(panel));

            model.deletePerson(first);
            assertEquals("2 outstanding customer stop(s) | 2 postal group(s)", totals(panel));
            assertEquals(List.of("POSTAL PREFIX 01", "POSTAL PREFIX 53"), groupHeaders(panel));
            assertEquals(List.of("Ben Tan", "Charlie Tan"), contactNames(panel));
        });
    }

    @Test
    public void missingFieldsAndLongText_remainVisibleAtMinimumWidth() throws Exception {
        runOnFxThread(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder().withAddress("A long delivery address ".repeat(8).strip())
                    .withPreorder(new PreorderBuilder().withMethod(FulfilmentMethod.DELIVERY)
                            .withItems(new ItemBuilder().build()).build()).build());
            DeliverySummaryPanel panel = new DeliverySummaryPanel(model.getFilteredPersonList());
            new Scene(panel.getRoot(), 410, 200);
            panel.getRoot().resize(410, 200);
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            assertTrue(text(panel).contains("MISSING POSTAL CODE"));
            assertTrue(text(panel).contains("Postal code needed"));
            assertTrue(text(panel).contains("Not scheduled"));
            assertTrue(text(panel).contains("Handover notes: None"));
            VBox stops = (VBox) panel.getRoot().lookup("#stops");
            for (Node node : stops.getChildren()) {
                Label label = (Label) node;
                assertTrue(label.isWrapText());
                assertTrue(label.getWidth() <= 410);
            }
        });
    }

    private static String totals(DeliverySummaryPanel panel) {
        return ((Label) panel.getRoot().lookup("#totals")).getText();
    }

    private static String text(DeliverySummaryPanel panel) {
        return ((VBox) panel.getRoot().lookup("#stops")).getChildren().stream()
                .map(node -> ((Label) node).getText()).collect(Collectors.joining("\n"));
    }

    private static List<String> groupHeaders(DeliverySummaryPanel panel) {
        return ((VBox) panel.getRoot().lookup("#stops")).getChildren().stream()
                .filter(node -> node.getStyleClass().contains("delivery-group"))
                .map(node -> ((Label) node).getText()).toList();
    }

    private static List<String> contactNames(DeliverySummaryPanel panel) {
        return ((VBox) panel.getRoot().lookup("#stops")).getChildren().stream()
                .filter(node -> node.getStyleClass().contains("delivery-stop"))
                .map(node -> ((Label) node).getText().split(" \\| ", 2)[0]).toList();
    }

    private static void runOnFxThread(Runnable action) throws Exception {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.run();
                result.complete(null);
            } catch (Throwable error) {
                result.completeExceptionally(error);
            }
        });
        result.get(10, TimeUnit.SECONDS);
    }
}
