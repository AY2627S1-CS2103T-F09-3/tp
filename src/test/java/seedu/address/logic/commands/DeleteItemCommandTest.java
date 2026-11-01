package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.PENDING_BENSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for
 * {@code DeleteItemCommand}.
 */
public class DeleteItemCommandTest {

    private static final Index INDEX_BENSON = INDEX_SECOND_PERSON;
    private static final Index INDEX_FIRST_ITEM = Index.fromOneBased(1);
    private static final Person EMPTY_BENSON = new PersonBuilder(BENSON).withPreorder().build();
    private static final Item PENDING_ITEM = PENDING_BENSON.getPreorder().orElseThrow().getItems().getFirst();
    private static final String EXPECTED_SUCCESS_MESSAGE = String.format(
            DeleteItemCommand.MESSAGE_DELETE_ITEM_SUCCESS, 1, BENSON.getName(), PENDING_ITEM.getName(),
            PENDING_ITEM.getQuantity(), PENDING_ITEM.getStatus());

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.setPerson(BENSON, PENDING_BENSON);
    }

    @Test
    public void execute_validIndexUnfilteredList_success() {
        DeleteItemCommand deleteItemCommand = new DeleteItemCommand(INDEX_BENSON, INDEX_FIRST_ITEM);

        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setPerson(BENSON, EMPTY_BENSON);

        assertCommandSuccess(deleteItemCommand, model, EXPECTED_SUCCESS_MESSAGE, expectedModel);
    }

    @Test
    public void execute_preorderWithSeveralItems_removesOnlySelectedItem() {
        Item first = new ItemBuilder().withName("Pineapple Tarts").withRemark("Less sugar").build();
        Item middle = new ItemBuilder().withName("Almond Cookies").build();
        Item last = new ItemBuilder().withName("Cake").withQuantity("1").build();
        List<Item> originalItems = List.of(first, middle, last);
        for (int position = 0; position < originalItems.size(); position++) {
            setUp();
            Person original = new PersonBuilder(BENSON).withPreorder(first, middle, last).build();
            model.setPerson(PENDING_BENSON, original);
            List<Item> remaining = new ArrayList<>(originalItems);
            Item removed = remaining.remove(position);
            Person expected = new PersonBuilder(BENSON).withPreorder(remaining.toArray(Item[]::new)).build();
            Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
            expectedModel.setPerson(BENSON, expected);
            String message = String.format(DeleteItemCommand.MESSAGE_DELETE_ITEM_SUCCESS, position + 1,
                    BENSON.getName(), removed.getName(), removed.getQuantity(), removed.getStatus());
            assertCommandSuccess(new DeleteItemCommand(INDEX_BENSON, Index.fromZeroBased(position)),
                    model, message, expectedModel);
            assertEquals(originalItems, original.getPreorder().orElseThrow().getItems());
        }
    }

    @Test
    public void execute_personWithoutPreorder_throwsCommandException() {
        DeleteItemCommand deleteItemCommand = new DeleteItemCommand(INDEX_FIRST_PERSON, INDEX_FIRST_ITEM);

        String expectedMessage = DeleteItemCommand.MESSAGE_NO_PREORDER;

        assertCommandFailure(deleteItemCommand, model, expectedMessage);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        DeleteItemCommand deleteItemCommand = new DeleteItemCommand(outOfBoundIndex, INDEX_FIRST_ITEM);

        assertCommandFailure(deleteItemCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_BENSON);
        DeleteItemCommand deleteItemCommand = new DeleteItemCommand(INDEX_FIRST_PERSON, INDEX_FIRST_ITEM);

        // the filter is kept, so the expected model still shows only Benson
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setPerson(BENSON, EMPTY_BENSON);
        showPersonAtIndex(expectedModel, INDEX_BENSON);

        assertCommandSuccess(deleteItemCommand, model, EXPECTED_SUCCESS_MESSAGE, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_BENSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        DeleteItemCommand deleteItemCommand = new DeleteItemCommand(outOfBoundIndex, INDEX_FIRST_ITEM);

        assertCommandFailure(deleteItemCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyPreorderOrInvalidItem_failureWithoutMutation() {
        assertCommandFailure(new DeleteItemCommand(INDEX_BENSON, Index.fromOneBased(2)), model,
                DeleteItemCommand.MESSAGE_INVALID_ITEM_INDEX);
        model.setPerson(PENDING_BENSON, EMPTY_BENSON);
        assertCommandFailure(new DeleteItemCommand(INDEX_BENSON, INDEX_FIRST_ITEM), model,
                DeleteItemCommand.MESSAGE_INVALID_ITEM_INDEX);
    }

    @Test
    public void execute_duplicateItems_removesOnlyOneOccurrence() {
        Person original = new PersonBuilder(BENSON).withPreorder(PENDING_ITEM, PENDING_ITEM).build();
        model.setPerson(PENDING_BENSON, original);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setPerson(BENSON, PENDING_BENSON);
        assertCommandSuccess(new DeleteItemCommand(INDEX_BENSON, INDEX_FIRST_ITEM),
                model, EXPECTED_SUCCESS_MESSAGE, expectedModel);
    }

    @Test
    public void execute_lastPendingItem_preservesStatusFilter() throws Exception {
        Item completed = new ItemBuilder().withStatus(ItemStatus.COMPLETED).build();
        Person original = new PersonBuilder(BENSON).withPreorder(PENDING_ITEM, completed).build();
        model.setPerson(PENDING_BENSON, original);
        new TrackCommand(Optional.of(ItemStatus.PENDING)).execute(model);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setPerson(BENSON, new PersonBuilder(BENSON).withPreorder(completed).build());
        new TrackCommand(Optional.of(ItemStatus.PENDING)).execute(expectedModel);
        assertCommandSuccess(new DeleteItemCommand(INDEX_FIRST_PERSON, INDEX_FIRST_ITEM),
                model, EXPECTED_SUCCESS_MESSAGE, expectedModel);
        assertFalse(model.getFilteredPersonList().contains(original));
    }

    @Test
    public void constructor_nullIndices_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DeleteItemCommand(null, INDEX_FIRST_ITEM));
        assertThrows(NullPointerException.class, () -> new DeleteItemCommand(INDEX_BENSON, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        DeleteItemCommand command = new DeleteItemCommand(INDEX_BENSON, INDEX_FIRST_ITEM);
        assertThrows(NullPointerException.class, () -> command.execute(null));
    }

    @Test
    public void execute_withRemainingItems_preservesPostalCodeAndFulfilment() throws Exception {
        for (FulfilmentMethod method : FulfilmentMethod.values()) {
            assertDetailsPreserved("012345", List.of(PENDING_ITEM, new ItemBuilder().withName("Cake").build()),
                    Optional.of(new FulfilmentDate("2026-12-20")), Optional.of(method),
                    Optional.of(new HandoverNotes("Call on arrival")));
        }
    }

    @Test
    public void execute_finalItem_preservesPostalCodeAndFulfilment() throws Exception {
        for (FulfilmentMethod method : FulfilmentMethod.values()) {
            assertDetailsPreserved("012345", List.of(PENDING_ITEM),
                    Optional.of(new FulfilmentDate("2026-12-20")), Optional.of(method),
                    Optional.of(new HandoverNotes("Call on arrival")));
        }
    }

    @Test
    public void execute_partialFulfilment_preservesPostalCodeAndMissingFields() throws Exception {
        assertDetailsPreserved("012345", List.of(PENDING_ITEM), Optional.of(new FulfilmentDate("2026-12-20")),
                Optional.empty(), Optional.empty());
        assertDetailsPreserved("012345", List.of(PENDING_ITEM), Optional.empty(),
                Optional.of(FulfilmentMethod.COLLECTION), Optional.empty());
        assertDetailsPreserved("012345", List.of(PENDING_ITEM), Optional.empty(), Optional.empty(),
                Optional.of(new HandoverNotes("Call on arrival")));
    }

    private void assertDetailsPreserved(String postalCode, List<Item> items, Optional<FulfilmentDate> date,
            Optional<FulfilmentMethod> method, Optional<HandoverNotes> notes) throws Exception {
        Preorder preorder = new Preorder(items, date, method, notes);
        Person original = new PersonBuilder(BENSON).withPostalCode(postalCode).withPreorder(preorder).build();
        Model actualModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        actualModel.setPerson(BENSON, original);
        showPersonAtIndex(actualModel, INDEX_BENSON);
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Preorder remaining = new Preorder(items.subList(1, items.size()), date, method, notes);
        expectedModel.setPerson(BENSON, new PersonBuilder(original).withPreorder(remaining).build());
        showPersonAtIndex(expectedModel, INDEX_BENSON);
        assertCommandSuccess(new DeleteItemCommand(INDEX_FIRST_PERSON, INDEX_FIRST_ITEM),
                actualModel, EXPECTED_SUCCESS_MESSAGE, expectedModel);
        assertEquals(items, preorder.getItems());
        assertEquals(preorder, original.getPreorder().orElseThrow());
    }

    @Test
    public void equals() {
        DeleteItemCommand deleteItemFirstCommand = new DeleteItemCommand(INDEX_FIRST_PERSON, INDEX_FIRST_ITEM);
        DeleteItemCommand deleteItemSecondCommand = new DeleteItemCommand(INDEX_SECOND_PERSON, INDEX_FIRST_ITEM);

        // same object -> returns true
        assertTrue(deleteItemFirstCommand.equals(deleteItemFirstCommand));

        // same values -> returns true
        DeleteItemCommand deleteItemFirstCommandCopy = new DeleteItemCommand(INDEX_FIRST_PERSON, INDEX_FIRST_ITEM);
        assertTrue(deleteItemFirstCommand.equals(deleteItemFirstCommandCopy));
        assertEquals(deleteItemFirstCommand.hashCode(), deleteItemFirstCommandCopy.hashCode());
        assertFalse(deleteItemFirstCommand.equals(
                new DeleteItemCommand(INDEX_FIRST_PERSON, Index.fromOneBased(2))));

        // different types -> returns false
        assertFalse(deleteItemFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteItemFirstCommand.equals(null));

        // different index -> returns false
        assertFalse(deleteItemFirstCommand.equals(deleteItemSecondCommand));
    }

    @Test
    public void toStringMethod() {
        Index targetIndex = Index.fromOneBased(1);
        DeleteItemCommand deleteItemCommand = new DeleteItemCommand(targetIndex, INDEX_FIRST_ITEM);
        String expected = DeleteItemCommand.class.getCanonicalName() + "{index=" + targetIndex
                + ", itemIndex=" + INDEX_FIRST_ITEM + "}";
        assertEquals(expected, deleteItemCommand.toString());
    }
}
