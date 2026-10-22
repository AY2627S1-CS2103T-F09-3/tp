package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.SearchCriteria;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

public class ProductSummaryPanelTest {
    private static final Item TARTS = new ItemBuilder().withName("Pineapple Tarts").withQuantity("2")
            .withRemark("Less sugar").build();
    private static final Person ALICE = new PersonBuilder().withName("Alice")
            .withPreorder(new ItemBuilder().withName("Cookies").build(), TARTS).build();

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another UI test already started JavaFX.
        }
    }

    @Test
    public void refresh_productSearch_showsCountsQuantityAndAttributedRemarks() throws Exception {
        onFxThread(() -> {
            Model model = createModel();
            ProductSummaryPanel panel = createPanel(model);
            execute(model, "find d/Pineapple Tarts");
            assertTrue(panel.getRoot().isVisible());
            assertTrue(panel.getRoot().isManaged());
            assertEquals("Preparation: Pineapple Tarts | All statuses", text(panel, "productHeading"));
            assertEquals("1 customer(s) | 1 matching item(s)", text(panel, "counts"));
            assertEquals("Total quantity: 2", text(panel, "quantity"));
            assertEquals("Alice, item 2 - Less sugar", text(panel, "remarks"));
        });
    }

    @Test
    public void refresh_sameCustomersDifferentCriteria_updatesHeading() throws Exception {
        onFxThread(() -> {
            Model model = createModel();
            ProductSummaryPanel panel = createPanel(model);
            execute(model, "find d/Pineapple Tarts");
            execute(model, "find d/Pineapple Tarts s/pending");
            assertEquals("Preparation: Pineapple Tarts | PENDING", text(panel, "productHeading"));
            execute(model, "find d/Cookies");
            assertEquals("Preparation: Cookies | All statuses", text(panel, "productHeading"));
            assertEquals("No special requests for matching items.", text(panel, "remarks"));
        });
    }

    @Test
    public void refresh_capitalizationOnlyChange_updatesHeadingIncludingNoMatches() throws Exception {
        onFxThread(() -> {
            Model model = createModel();
            ProductSummaryPanel panel = createPanel(model);
            execute(model, "find d/Pineapple Tarts");
            execute(model, "find d/  PINEAPPLE TARTS  ");
            assertEquals("Preparation: PINEAPPLE TARTS | All statuses", text(panel, "productHeading"));
            assertEquals("1 customer(s) | 1 matching item(s)", text(panel, "counts"));
            execute(model, "find d/Unknown Product");
            execute(model, "find d/UNKNOWN PRODUCT");
            assertEquals("Preparation: UNKNOWN PRODUCT | All statuses", text(panel, "productHeading"));
            assertEquals("0 customer(s) | 0 matching item(s)", text(panel, "counts"));
        });
    }

    @Test
    public void refresh_withoutProduct_hidesPanelAndRemovesLayoutSpace() throws Exception {
        onFxThread(() -> {
            Model model = createModel();
            ProductSummaryPanel panel = createPanel(model);
            assertFalse(panel.getRoot().isVisible());
            for (String command : List.of("list", "find n/Alice", "track", "find s/pending")) {
                execute(model, "find d/Pineapple Tarts");
                execute(model, command);
                assertFalse(panel.getRoot().isVisible());
                assertFalse(panel.getRoot().isManaged());
            }
        });
    }

    @Test
    public void refresh_productWithNoStatuses_showsEmptySelection() throws Exception {
        onFxThread(() -> {
            Model model = createModel();
            ProductSummaryPanel panel = createPanel(model);
            model.updateFilteredPersonList(new SearchCriteria(List.of(
                    new ItemCriterion(Optional.of("Pineapple Tarts"), Set.of()))));

            assertEquals("Preparation: Pineapple Tarts | No statuses", text(panel, "productHeading"));
            assertEquals("0 customer(s) | 0 matching item(s)", text(panel, "counts"));
        });
    }

    @Test
    public void refresh_matchingCustomerChanged_updatesQuantityAndRemarks() throws Exception {
        onFxThread(() -> {
            Model model = createModel();
            ProductSummaryPanel panel = createPanel(model);
            execute(model, "find d/Pineapple Tarts");
            Person changed = new PersonBuilder(ALICE).withPreorder(new ItemBuilder(TARTS)
                    .withoutQuantity().withRemark("Separate packaging").build()).build();
            model.setPerson(ALICE, changed);
            assertEquals("Known quantity: 0 | 1 item(s) need a quantity", text(panel, "quantity"));
            assertEquals("Alice, item 1 - Separate packaging", text(panel, "remarks"));
            model.deletePerson(changed);
            assertEquals("0 customer(s) | 0 matching item(s)", text(panel, "counts"));
            assertEquals("Total quantity: 0", text(panel, "quantity"));
            assertTrue(panel.getRoot().isVisible());
        });
    }

    @Test
    public void refresh_newCustomerAndCustomerFilter_respectsDisplayedSubset() throws Exception {
        onFxThread(() -> {
            Model model = createModel();
            ProductSummaryPanel panel = createPanel(model);
            execute(model, "find d/Pineapple Tarts");
            model.addPerson(new PersonBuilder().withName("Ben").withPreorder(TARTS).build());
            assertEquals("2 customer(s) | 2 matching item(s)", text(panel, "counts"));
            execute(model, "find n/Alice d/Pineapple Tarts");
            assertEquals("1 customer(s) | 1 matching item(s)", text(panel, "counts"));
        });
    }

    @Test
    public void layout_shortContent_usesContentHeightAndGrowsForMoreRemarks() throws Exception {
        onFxThread(() -> {
            Model model = createModel();
            execute(model, "find d/Pineapple Tarts");
            ScrollPane scroll = (ScrollPane) createPanel(model).getRoot();
            VBox root = new VBox(scroll);
            new Scene(root, 800, 800).getStylesheets().add(
                    ProductSummaryPanel.class.getResource("/view/DarkTheme.css").toExternalForm());
            root.applyCss();
            root.layout();
            double shortHeight = scroll.getHeight();
            assertTrue(shortHeight < 180);
            assertEquals(scroll.getContent().getBoundsInLocal().getHeight(),
                    scroll.getViewportBounds().getHeight(), 2);
            KeyEvent noScroll = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.DOWN,
                    false, false, false, false);
            scroll.fireEvent(noScroll);
            assertFalse(noScroll.isConsumed());

            model.setPerson(ALICE, new PersonBuilder(ALICE).withPreorder(TARTS, TARTS, TARTS, TARTS, TARTS).build());
            root.layout();
            assertTrue(scroll.getHeight() > shortHeight);
            assertTrue(scroll.getHeight() <= 180);
            execute(model, "find d/Cookies");
            root.layout();
            assertTrue(scroll.getHeight() < 180);
        });
    }

    @Test
    public void layout_longContent_wrapsAndSupportsKeyboardScrolling() throws Exception {
        onFxThread(() -> {
            Model model = createModel();
            String product = "Pineapple Tarts ".repeat(12).trim();
            Item longRemark = new ItemBuilder(TARTS).withName(product)
                    .withRemark("Long request ".repeat(7).trim()).build();
            model.setPerson(ALICE, new PersonBuilder(ALICE)
                    .withPreorder(longRemark, longRemark, longRemark, longRemark, longRemark).build());
            execute(model, "find d/" + product);
            ProductSummaryPanel panel = createPanel(model);
            ScrollPane scroll = (ScrollPane) panel.getRoot();
            VBox root = new VBox(scroll);
            new Scene(root, 450, 400);
            root.applyCss();
            root.resize(450, 400);
            root.layout();
            assertTrue(((Label) root.lookup("#remarks")).isWrapText());
            assertTrue(scroll.isFocusTraversable());
            assertTrue(scroll.getHeight() <= 180);
            assertTrue(scroll.getContent().getBoundsInLocal().getHeight() > scroll.getViewportBounds().getHeight());
            scroll.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.PAGE_DOWN,
                    false, false, false, false));
            assertTrue(scroll.getVvalue() > 0);
            press(scroll, KeyCode.END);
            assertEquals(1, scroll.getVvalue());
            press(scroll, KeyCode.UP);
            assertTrue(scroll.getVvalue() < 1);
            press(scroll, KeyCode.HOME);
            assertEquals(0, scroll.getVvalue());
            press(scroll, KeyCode.DOWN);
            assertTrue(scroll.getVvalue() > 0);
            press(scroll, KeyCode.PAGE_UP);
            assertEquals(0, scroll.getVvalue());
            double previous = scroll.getVvalue();
            KeyEvent unrelated = press(scroll, KeyCode.RIGHT);
            assertFalse(unrelated.isConsumed());
            assertEquals(previous, scroll.getVvalue());
        });
    }

    @Test
    public void refresh_postalGroupingAndItemEdit_keepsSummaryAndHighlightsConsistent() throws Exception {
        onFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder(ALICE).withPostalCode("019999").build());
            model.addPerson(new PersonBuilder(ALICE).withName("Ben").withPostalCode("012345").build());
            model.addPerson(new PersonBuilder(ALICE).withName("Other Area").withPostalCode("530123").build());
            ProductSummaryPanel summary = createPanel(model);
            PersonListPanel list = new PersonListPanel(model.getFilteredPersonList(),
                    model.activeSearchCriteriaProperty());
            VBox root = new VBox(summary.getRoot(), list.getRoot());
            new Scene(root, 800, 1200);
            execute(model, "find c/01 d/Pineapple Tarts s/pending");
            execute(model, "group postal");
            root.applyCss();
            root.layout();
            assertEquals("2 customer(s) | 2 matching item(s)", text(summary, "counts"));
            assertEquals("Total quantity: 4", text(summary, "quantity"));
            assertEquals(2, root.lookupAll("." + PersonCard.MATCH_STYLE_CLASS).size());
            execute(model, "edititem 1 i/2 s/completed");
            root.layout();
            assertEquals("Alice", model.getFilteredPersonList().getFirst().getName().fullName);
            assertEquals("1 customer(s) | 1 matching item(s)", text(summary, "counts"));
            assertEquals(1, root.lookupAll("." + PersonCard.MATCH_STYLE_CLASS).size());
            execute(model, "list");
            root.layout();
            assertFalse(summary.getRoot().isVisible());
            assertEquals(0, root.lookupAll("." + PersonCard.MATCH_STYLE_CLASS).size());
        });
    }

    private Model createModel() {
        return new ModelManager(new AddressBookBuilder().withPerson(ALICE).build(), new UserPrefs());
    }

    private ProductSummaryPanel createPanel(Model model) {
        return new ProductSummaryPanel(model.getFilteredPersonList(), model.activeSearchCriteriaProperty());
    }

    private void execute(Model model, String command) throws Exception {
        new AddressBookParser().parseCommand(command).execute(model);
    }

    private String text(ProductSummaryPanel panel, String id) {
        return ((Label) ((ScrollPane) panel.getRoot()).getContent().lookup("#" + id)).getText();
    }

    private KeyEvent press(ScrollPane scroll, KeyCode key) {
        KeyEvent event = new KeyEvent(KeyEvent.KEY_PRESSED, "", "", key,
                false, false, false, false);
        scroll.fireEvent(event);
        return event;
    }

    private void onFxThread(FxAction action) throws Exception {
        CompletableFuture<Void> completion = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.run();
                completion.complete(null);
            } catch (Throwable error) {
                completion.completeExceptionally(error);
            }
        });
        completion.get(10, TimeUnit.SECONDS);
    }

    @FunctionalInterface
    private interface FxAction {
        void run() throws Exception;
    }
}
