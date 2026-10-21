package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks how a {@code PersonCard} shows a customer's preorder.
 * Starting JavaFX needs a display; on Linux CI runners, one is provided by xvfb (see gradle.yml).
 */
public class PersonCardTest {

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // JavaFX was already started by an earlier test class
        }
    }

    @Test
    public void constructor_personWithItems_showsOneChipPerItem() throws Exception {
        Item tarts = new ItemBuilder().withName("Pineapple Tarts").withRemark("Less sugar").build();
        Item cookies = new ItemBuilder().withName("Almond Cookies").withQuantity("1")
                .withStatus(ItemStatus.PREPARED).build();
        PersonCard card = createCard(new PersonBuilder(ALICE).withPostalCode("012345")
                .withPreorder(tarts, cookies).build());

        assertEquals("012345", ((Label) lookup(card, "#postalCode")).getText());
        assertTrue(lookup(card, "#preorder").isVisible());
        assertTrue(lookup(card, "#preorder").isManaged());
        assertEquals(List.of(PreorderDisplay.formatItem(1, tarts), PreorderDisplay.formatItem(2, cookies)),
                getChips(card).stream().map(Label::getText).toList());
    }

    @Test
    public void constructor_eachStatus_usesMatchingColourClass() throws Exception {
        for (ItemStatus status : ItemStatus.values()) {
            Item item = new ItemBuilder().withStatus(status).build();
            Label chip = getChips(createCard(new PersonBuilder(ALICE).withPreorder(item).build())).get(0);

            assertTrue(chip.getStyleClass().contains(PreorderDisplay.getStatusStyleClass(status)));
        }
    }

    @Test
    public void constructor_preorderWithoutItems_showsNoItemsChip() throws Exception {
        PersonCard card = createCard(new PersonBuilder(ALICE).withPreorder().build());

        List<Label> chips = getChips(card);
        assertEquals(1, chips.size());
        assertEquals(PreorderDisplay.NO_ITEMS_TEXT, chips.get(0).getText());
        assertTrue(chips.get(0).getStyleClass().contains(PreorderDisplay.NO_ITEMS_STYLE_CLASS));
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

    /**
     * Builds the card on the JavaFX thread, as a real list cell would.
     */
    private static PersonCard createCard(Person person) throws Exception {
        CompletableFuture<PersonCard> card = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                card.complete(new PersonCard(person, 1));
            } catch (RuntimeException e) {
                card.completeExceptionally(e);
            }
        });
        return card.get(10, TimeUnit.SECONDS);
    }

    private static List<Label> getChips(PersonCard card) {
        VBox preorder = (VBox) lookup(card, "#preorder");
        return preorder.getChildren().stream()
                .map(chip -> (Label) chip)
                .toList();
    }

    private static Node lookup(PersonCard card, String selector) {
        Node node = card.getRoot().lookup(selector);
        assertTrue(node != null, "missing node " + selector);
        return node;
    }
}
