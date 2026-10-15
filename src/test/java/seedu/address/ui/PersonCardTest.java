package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalPersons.ALICE;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.PreorderStatus;
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
    public void constructor_personWithPreorder_showsOrderAndStatusChips() throws Exception {
        Person person = new PersonBuilder(ALICE).withPreorder("2 chocolate cakes", PreorderStatus.PENDING).build();
        PersonCard card = createCard(person);

        assertTrue(lookup(card, "#preorder").isVisible());
        assertTrue(lookup(card, "#preorder").isManaged());
        assertEquals("Order: 2 chocolate cakes", ((Label) lookup(card, "#preorderDetails")).getText());
        assertEquals("Status: PENDING", ((Label) lookup(card, "#preorderStatus")).getText());
    }

    @Test
    public void constructor_eachStatus_usesMatchingColourClass() throws Exception {
        for (PreorderStatus status : PreorderStatus.values()) {
            Person person = new PersonBuilder(ALICE).withPreorder("2 chocolate cakes", status).build();
            Node statusChip = lookup(createCard(person), "#preorderStatus");

            assertTrue(statusChip.getStyleClass().contains(PreorderDisplay.getStatusStyleClass(status)));
        }
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

    private static Node lookup(PersonCard card, String selector) {
        Node node = card.getRoot().lookup(selector);
        assertTrue(node != null, "missing node " + selector);
        return node;
    }
}
