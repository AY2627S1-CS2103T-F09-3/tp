package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.PENDING_BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteOrderCommand}.
 */
public class DeleteOrderCommandTest {

    private static final Index INDEX_BENSON = INDEX_SECOND_PERSON;
    private static final String EXPECTED_SUCCESS_MESSAGE = String.format(
            DeleteOrderCommand.MESSAGE_DELETE_ORDER_SUCCESS, BENSON.getName());

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.setPerson(BENSON, PENDING_BENSON);
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        DeleteOrderCommand deleteOrderCommand = new DeleteOrderCommand(INDEX_BENSON);

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        assertCommandSuccess(deleteOrderCommand, model, EXPECTED_SUCCESS_MESSAGE, expectedModel);
    }

    @Test
    public void execute_preorderWithSeveralItems_deletesWholePreorder() {
        Person bensonWithItems = new PersonBuilder(BENSON).withPreorder(
                new ItemBuilder().withName("Pineapple Tarts").build(),
                new ItemBuilder().withName("Almond Cookies").build()).build();
        model.setPerson(PENDING_BENSON, bensonWithItems);

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

        assertCommandSuccess(new DeleteOrderCommand(INDEX_BENSON), model, EXPECTED_SUCCESS_MESSAGE, expectedModel);
    }

    @Test
    public void execute_personWithoutPreorder_throwsCommandException() {
        DeleteOrderCommand deleteOrderCommand = new DeleteOrderCommand(INDEX_FIRST_PERSON);

        String expectedMessage = String.format(DeleteOrderCommand.MESSAGE_NO_PREORDER, ALICE.getName());

        assertCommandFailure(deleteOrderCommand, model, expectedMessage);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteOrderCommand deleteOrderCommand = new DeleteOrderCommand(outOfBoundIndex);

        assertCommandFailure(deleteOrderCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_BENSON);
        DeleteOrderCommand deleteOrderCommand = new DeleteOrderCommand(INDEX_FIRST_PERSON);

        // the filter is kept, so the expected model still shows only Benson
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        showPersonAtIndex(expectedModel, INDEX_BENSON);

        assertCommandSuccess(deleteOrderCommand, model, EXPECTED_SUCCESS_MESSAGE, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_BENSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteOrderCommand deleteOrderCommand = new DeleteOrderCommand(outOfBoundIndex);

        assertCommandFailure(deleteOrderCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        DeleteOrderCommand deleteOrderFirstCommand = new DeleteOrderCommand(INDEX_FIRST_PERSON);
        DeleteOrderCommand deleteOrderSecondCommand = new DeleteOrderCommand(INDEX_SECOND_PERSON);

        // same object -> returns true
        assertTrue(deleteOrderFirstCommand.equals(deleteOrderFirstCommand));

        // same values -> returns true
        DeleteOrderCommand deleteOrderFirstCommandCopy = new DeleteOrderCommand(INDEX_FIRST_PERSON);
        assertTrue(deleteOrderFirstCommand.equals(deleteOrderFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteOrderFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteOrderFirstCommand.equals(null));

        // different index -> returns false
        assertFalse(deleteOrderFirstCommand.equals(deleteOrderSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteOrderCommand deleteOrderCommand = new DeleteOrderCommand(targetIndex);
        String expected = DeleteOrderCommand.class.getCanonicalName() + "{index=" + targetIndex + "}";
        assertEquals(expected, deleteOrderCommand.toString());
    }
}
