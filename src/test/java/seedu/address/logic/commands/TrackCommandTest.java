package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.preorder.PreorderStatus.COMPLETED;
import static seedu.address.model.preorder.PreorderStatus.PENDING;
import static seedu.address.model.preorder.PreorderStatus.PREPARED;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.PreorderStatus;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests track command execution with the Model and command value semantics.
 */
public class TrackCommandTest {

    private static final Person PENDING_BENSON =
            new PersonBuilder(BENSON).withPreorder("2 chocolate cakes", PENDING).build();
    private static final Person PREPARED_CARL =
            new PersonBuilder(CARL).withPreorder("12 cupcakes", PREPARED).build();
    private static final Person COMPLETED_DANIEL =
            new PersonBuilder(DANIEL).withPreorder("50 pineapple tarts", COMPLETED).build();
    private static final Person PENDING_ELLE =
            new PersonBuilder(ELLE).withPreorder("1 strawberry cake", PENDING).build();

    @Test
    public void constructor_nullStatus_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new TrackCommand(null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        TrackCommand command = new TrackCommand(Optional.empty());
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_noStatus_listsActivePreordersInOriginalOrder() {
        assertTrackResult(new TrackCommand(Optional.empty()), createMixedModel(), "3 order(s) listed!",
                List.of(PENDING_ELLE, PREPARED_CARL, PENDING_BENSON));
    }

    @Test
    public void execute_pendingStatus_listsPendingPreordersInOriginalOrder() {
        assertTrackResult(new TrackCommand(Optional.of(PENDING)), createMixedModel(),
                "2 order(s) for PENDING listed!", List.of(PENDING_ELLE, PENDING_BENSON));
    }

    @Test
    public void execute_preparedStatus_listsPreparedPreorders() {
        assertTrackResult(new TrackCommand(Optional.of(PREPARED)), createMixedModel(),
                "1 order(s) for PREPARED listed!", List.of(PREPARED_CARL));
    }

    @Test
    public void execute_completedStatus_listsCompletedPreorders() {
        assertTrackResult(new TrackCommand(Optional.of(COMPLETED)), createMixedModel(),
                "1 order(s) for COMPLETED listed!", List.of(COMPLETED_DANIEL));
    }

    @Test
    public void execute_emptyAddressBook_listsNoPreorders() {
        assertTrackResult(new TrackCommand(Optional.empty()), createModel(), "0 order(s) listed!", List.of());
        for (PreorderStatus status : PreorderStatus.values()) {
            assertTrackResult(new TrackCommand(Optional.of(status)), createModel(), "0 order(s) listed!", List.of());
        }
    }

    @Test
    public void execute_customersWithoutPreorders_listsNoPreorders() {
        Model model = createModel(ALICE, BENSON);
        assertTrackResult(new TrackCommand(Optional.empty()), model, "0 order(s) listed!", List.of());
        for (PreorderStatus status : PreorderStatus.values()) {
            assertTrackResult(new TrackCommand(Optional.of(status)), model, "0 order(s) listed!", List.of());
        }
    }

    @Test
    public void execute_noActivePreorders_listsNoPreorders() {
        assertTrackResult(new TrackCommand(Optional.empty()), createModel(ALICE, COMPLETED_DANIEL),
                "0 order(s) listed!", List.of());
    }

    @Test
    public void execute_noMatchingStatus_listsNoPreorders() {
        assertTrackResult(new TrackCommand(Optional.of(COMPLETED)), createModel(ALICE, PENDING_BENSON, PREPARED_CARL),
                "0 order(s) listed!", List.of());
    }

    @Test
    public void execute_noStatus_replacesExistingFilter() {
        Model model = createMixedModel();
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        assertEquals(List.of(ALICE), model.getFilteredPersonList());

        assertTrackResult(new TrackCommand(Optional.empty()), model, "3 order(s) listed!",
                List.of(PENDING_ELLE, PREPARED_CARL, PENDING_BENSON));
    }

    @Test
    public void execute_explicitStatus_replacesExistingFilter() {
        Model model = createMixedModel();
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Carl")));
        assertEquals(List.of(PREPARED_CARL), model.getFilteredPersonList());

        assertTrackResult(new TrackCommand(Optional.of(PENDING)), model,
                "2 order(s) for PENDING listed!", List.of(PENDING_ELLE, PENDING_BENSON));
    }

    @Test
    public void execute_differentStatus_replacesPreviousTrackFilter() {
        Model model = createMixedModel();
        assertTrackResult(new TrackCommand(Optional.of(COMPLETED)), model,
                "1 order(s) for COMPLETED listed!", List.of(COMPLETED_DANIEL));
        assertTrackResult(new TrackCommand(Optional.of(PENDING)), model,
                "2 order(s) for PENDING listed!", List.of(PENDING_ELLE, PENDING_BENSON));
        assertTrackResult(new TrackCommand(Optional.empty()), model, "3 order(s) listed!",
                List.of(PENDING_ELLE, PREPARED_CARL, PENDING_BENSON));
    }

    @Test
    public void equals() {
        TrackCommand activeCommand = new TrackCommand(Optional.empty());
        TrackCommand pendingCommand = new TrackCommand(Optional.of(PENDING));

        assertTrue(activeCommand.equals(activeCommand));
        assertTrue(activeCommand.equals(new TrackCommand(Optional.empty())));
        assertTrue(pendingCommand.equals(new TrackCommand(Optional.of(PENDING))));
        assertFalse(activeCommand.equals(pendingCommand));
        assertFalse(pendingCommand.equals(new TrackCommand(Optional.of(PREPARED))));
        assertFalse(pendingCommand.equals(new TrackCommand(Optional.of(COMPLETED))));
        assertFalse(activeCommand.equals(null));
        assertFalse(activeCommand.equals(1));
    }

    @Test
    public void hashCode_sameStatus_sameHashCode() {
        assertEquals(new TrackCommand(Optional.empty()).hashCode(), new TrackCommand(Optional.empty()).hashCode());
        assertEquals(new TrackCommand(Optional.of(PENDING)).hashCode(),
                new TrackCommand(Optional.of(PENDING)).hashCode());
    }

    @Test
    public void toStringMethod() {
        TrackCommand activeCommand = new TrackCommand(Optional.empty());
        assertEquals(TrackCommand.class.getCanonicalName() + "{status=Optional.empty}", activeCommand.toString());

        TrackCommand pendingCommand = new TrackCommand(Optional.of(PENDING));
        assertEquals(TrackCommand.class.getCanonicalName() + "{status=Optional[PENDING]}", pendingCommand.toString());
    }

    private Model createMixedModel() {
        return createModel(COMPLETED_DANIEL, PENDING_ELLE, ALICE, PREPARED_CARL, PENDING_BENSON);
    }

    private Model createModel(Person... persons) {
        AddressBookBuilder builder = new AddressBookBuilder();
        for (Person person : persons) {
            builder.withPerson(person);
        }
        return new ModelManager(builder.build(), new UserPrefs());
    }

    private void assertTrackResult(TrackCommand command, Model model, String expectedMessage,
            List<Person> expectedPersons) {
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());

        assertEquals(new CommandResult(expectedMessage), command.execute(model));
        assertEquals(expectedPersons, model.getFilteredPersonList());
        assertEquals(originalAddressBook, model.getAddressBook());
    }
}
