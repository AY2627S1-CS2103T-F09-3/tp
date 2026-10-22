package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.SearchCriteria;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PreorderBuilder;

/**
 * Checks how a {@code PersonCard} shows a customer's preorder.
 * Starting JavaFX needs a display; on Linux CI runners, one is provided by xvfb (see gradle.yml).
 */
public class PersonCardTest {

    // Narrow enough that a long remark has to wrap
    private static final double CARD_WIDTH = 300;

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX was already started by an earlier test class
        }
    }

    @Test
    public void constructor_personWithItems_showsOneRowPerItem() throws Exception {
        Item tarts = new ItemBuilder().withName("Pineapple Tarts").withRemark("Less sugar").build();
        Item cookies = new ItemBuilder().withName("Almond Cookies").withQuantity("1")
                .withStatus(ItemStatus.PREPARED).build();
        PersonCard card = createCard(new PersonBuilder(ALICE).withPostalCode("012345")
                .withPreorder(tarts, cookies).build());

        assertEquals("012345", ((Label) lookup(card, "#postalCode")).getText());
        assertTrue(lookup(card, "#preorder").isVisible());
        assertTrue(lookup(card, "#preorder").isManaged());

        List<GridPane> rows = getRows(card);
        assertEquals(2, rows.size());
        assertEquals(List.of("1. Pineapple Tarts", "Qty 2", "PENDING", "Remark: Less sugar"), getTexts(rows.get(0)));
        assertEquals(List.of("2. Almond Cookies", "Qty 1", "PREPARED"), getTexts(rows.get(1)));
    }

    @Test
    public void constructor_preorderWithFulfilmentDetails_showsThemAboveItems() throws Exception {
        Preorder preorder = new PreorderBuilder().withItems(new ItemBuilder().build()).withDate("2026-12-20")
                .withMethod(FulfilmentMethod.DELIVERY).withHandoverNotes("Call on arrival").build();
        PersonCard card = createCard(new PersonBuilder(ALICE).withPreorder(preorder).build());

        assertEquals("20 Dec 2026 | Delivery", ((Label) lookup(card, "#preorderHeader")).getText());
        assertEquals(PreorderDisplay.DELIVERY_ICON, ((SVGPath) lookup(card, "#preorderIcon")).getContent());
        Node handoverNotes = lookup(card, "#handoverNotes");
        assertEquals("Handover notes: Call on arrival", ((Label) handoverNotes).getText());
        assertTrue(handoverNotes.isManaged());
    }

    @Test
    public void constructor_preorderWithoutFulfilmentDetails_notScheduledAndNoNotesLine() throws Exception {
        PersonCard card = createCard(new PersonBuilder(ALICE).withPreorder(new ItemBuilder().build()).build());

        assertEquals("Not scheduled | Method not set", ((Label) lookup(card, "#preorderHeader")).getText());
        Node handoverNotes = lookup(card, "#handoverNotes");
        assertFalse(handoverNotes.isVisible());
        assertFalse(handoverNotes.isManaged());
    }

    @Test
    public void constructor_eachStatus_usesMatchingColorClass() throws Exception {
        for (ItemStatus status : ItemStatus.values()) {
            Item item = new ItemBuilder().withStatus(status).build();
            GridPane row = getRows(createCard(new PersonBuilder(ALICE).withPreorder(item).build())).get(0);
            Node chip = row.lookup("." + PersonCard.STATUS_CHIP_STYLE_CLASS);

            assertTrue(chip.getStyleClass().contains(PreorderDisplay.getStatusStyleClass(status)));
        }
    }

    @Test
    public void constructor_itemWithoutQuantity_showsQuantityNeeded() throws Exception {
        Item cakes = new ItemBuilder().withName("2 chocolate cakes").withoutQuantity().build();
        GridPane row = getRows(createCard(new PersonBuilder(ALICE).withPreorder(cakes).build())).get(0);
        Label quantity = (Label) row.lookup("." + PersonCard.MISSING_QUANTITY_STYLE_CLASS);

        assertEquals(PreorderDisplay.MISSING_QUANTITY_TEXT, quantity.getText());
    }

    @Test
    public void constructor_preorderWithoutItems_showsNoItemsText() throws Exception {
        PersonCard card = createCard(new PersonBuilder(ALICE).withPreorder().build());

        VBox items = (VBox) lookup(card, "#preorderItems");
        assertEquals(1, items.getChildren().size());
        Label noItems = (Label) items.getChildren().get(0);
        assertEquals(PreorderDisplay.NO_ITEMS_TEXT, noItems.getText());
        assertTrue(noItems.getStyleClass().contains(PersonCard.NO_ITEMS_STYLE_CLASS));
    }

    @Test
    public void constructor_personWithoutPreorder_hidesPreorderSection() throws Exception {
        PersonCard card = createCard(new PersonBuilder(ALICE).build());

        Node preorder = lookup(card, "#preorder");
        assertFalse(preorder.isVisible());
        // not managed, so the hidden section leaves no blank gap in the card
        assertFalse(preorder.isManaged());
    }

    @Test
    public void constructor_anyPerson_keepsContactDetails() throws Exception {
        PersonCard card = createCard(new PersonBuilder(ALICE).build());

        assertEquals(ALICE.getName().fullName, ((Label) lookup(card, "#name")).getText());
        assertEquals(ALICE.getPhone().value, ((Label) lookup(card, "#phone")).getText());
        assertEquals(ALICE.getAddress().value, ((Label) lookup(card, "#address")).getText());
        assertEquals(ALICE.getEmail().value, ((Label) lookup(card, "#email")).getText());
    }

    @Test
    public void prefHeight_wrappingRemarkAndNotes_sameAsContent() throws Exception {
        Item tarts = new ItemBuilder().withRemark("Less sugar, and pack the tarts in two separate boxes for gifting")
                .build();
        Preorder preorder = new PreorderBuilder().withItems(tarts)
                .withHandoverNotes("Leave the boxes with the concierge at the lobby if nobody answers").build();
        PersonCard card = createCard(new PersonBuilder(ALICE).withPreorder(preorder).build());

        double[] heights = onFxThread(() -> {
            Region root = card.getRoot();
            new Scene(root).getStylesheets().add(PersonCard.class.getResource("/view/DarkTheme.css").toExternalForm());
            root.applyCss();
            Region content = (Region) root.getChildrenUnmodifiable().get(0);
            return new double[] {root.prefHeight(CARD_WIDTH), content.prefHeight(CARD_WIDTH)};
        });

        // A card that asks for more height than its content leaves a blank gap at the bottom of its list cell
        assertEquals(heights[1], heights[0], 0.5);
    }

    @Test
    public void constructor_productAndStatus_marksOnlyMatchingItemsAndKeepsNumbering() throws Exception {
        Item pending = new ItemBuilder().withName("Pineapple Tarts").withStatus(ItemStatus.PENDING).build();
        Item prepared = new ItemBuilder(pending).withStatus(ItemStatus.PREPARED).build();
        Item other = new ItemBuilder().withName("Cookies").withStatus(ItemStatus.PENDING).build();
        Person person = new PersonBuilder(ALICE).withPreorder(prepared, pending, other, pending).build();
        SearchCriteria criteria = new SearchCriteria(List.of(new ItemCriterion(
                Optional.of("PINEAPPLE TARTS"), Set.of(ItemStatus.PENDING))));
        List<GridPane> chips = getRows(createCard(person, criteria));

        assertEquals(4, chips.size());
        for (int i = 0; i < chips.size(); i++) {
            boolean matches = i == 1 || i == 3;
            assertEquals(matches, chips.get(i).getStyleClass().contains(PersonCard.MATCH_STYLE_CLASS));
            Item item = List.of(prepared, pending, other, pending).get(i);
            Label name = (Label) chips.get(i).getChildren().getFirst();
            assertEquals(PreorderDisplay.formatItemName(i + 1, item),
                    name.getText());
            assertTrue(chips.get(i).getChildren().get(2).getStyleClass()
                    .contains(PreorderDisplay.getStatusStyleClass(item.getStatus())));
        }
    }

    @Test
    public void constructor_productWithoutStatus_marksEveryStatus() throws Exception {
        for (ItemStatus status : ItemStatus.values()) {
            Person person = new PersonBuilder(ALICE).withPreorder(
                    new ItemBuilder().withName("Tarts").withStatus(status).build()).build();
            SearchCriteria criteria = new SearchCriteria(List.of(new ItemCriterion(
                    Optional.of("tarts"), Set.of(ItemStatus.values()))));
            assertTrue(getRows(createCard(person, criteria)).get(0).getStyleClass()
                    .contains(PersonCard.MATCH_STYLE_CLASS));
        }
    }

    /**
     * Builds the card on the JavaFX thread, as a real list cell would.
     */
    private static PersonCard createCard(Person person) throws Exception {
        return onFxThread(() -> new PersonCard(person, 1));
    }

    private static PersonCard createCard(Person person, SearchCriteria criteria) throws Exception {
        return onFxThread(() -> new PersonCard(person, 1, criteria));
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

    private static List<GridPane> getRows(PersonCard card) {
        VBox items = (VBox) lookup(card, "#preorderItems");
        return items.getChildren().stream()
                .map(row -> (GridPane) row)
                .toList();
    }

    private static List<String> getTexts(GridPane row) {
        return row.getChildren().stream()
                .map(label -> ((Label) label).getText())
                .toList();
    }

    private static Node lookup(PersonCard card, String selector) {
        Node node = card.getRoot().lookup(selector);
        assertTrue(node != null, "missing node " + selector);
        return node;
    }
}
