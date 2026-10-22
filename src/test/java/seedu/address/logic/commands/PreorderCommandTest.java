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
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.PreorderCommand.EditFulfilmentDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.testutil.EditFulfilmentDescriptorBuilder;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PreorderBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code PreorderCommand}.
 */
public class PreorderCommandTest {

    private static final Index INDEX_BENSON = INDEX_SECOND_PERSON;
    private static final String PAST_DATE = "2020-01-01";
    private static final String FUTURE_DATE = "2099-12-31";

    private static final Item PINEAPPLE_TARTS = new ItemBuilder().withName("Pineapple Tarts").withQuantity("3")
            .withRemark("Less sugar").build();
    private static final Item ALMOND_COOKIES = new ItemBuilder().withName("Almond Cookies")
            .withStatus(ItemStatus.PREPARED).build();

    // Benson's preorder has items and every fulfilment detail, so tests can check what is kept
    private static final Preorder BENSON_PREORDER = new PreorderBuilder().withItems(PINEAPPLE_TARTS, ALMOND_COOKIES)
            .withDate(FUTURE_DATE).withMethod(FulfilmentMethod.COLLECTION)
            .withHandoverNotes("Collect from side gate").build();
    private static final Person BENSON_WITH_PREORDER = new PersonBuilder(BENSON).withPostalCode("012345")
            .withPreorder(BENSON_PREORDER).build();

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        model.setPerson(BENSON, BENSON_WITH_PREORDER);
    }

    /**
     * Asserts that running {@code descriptor} on the customer at {@code index} gives {@code person} the
     * {@code expectedPreorder}, keeps their other details, and shows the success message.
     */
    private void assertPreorderUpdated(Index index, Person person, EditFulfilmentDescriptor descriptor,
            Preorder expectedPreorder) {
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(person, new PersonBuilder(person).withPreorder(expectedPreorder).build());
        String expectedMessage = String.format(PreorderCommand.MESSAGE_SUCCESS, person.getName(),
                Messages.formatFulfilmentDetails(expectedPreorder));

        assertCommandSuccess(new PreorderCommand(index, descriptor), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_customerWithoutPreorder_createsPreorderWithNoItems() {
        EditFulfilmentDescriptor descriptor = new EditFulfilmentDescriptorBuilder().withDate(FUTURE_DATE)
                .withMethod(FulfilmentMethod.DELIVERY).withHandoverNotes("Call on arrival").build();

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        Preorder expectedPreorder = new PreorderBuilder().withDate(FUTURE_DATE).withMethod(FulfilmentMethod.DELIVERY)
                .withHandoverNotes("Call on arrival").build();
        expectedModel.setPerson(ALICE, new PersonBuilder(ALICE).withPreorder(expectedPreorder).build());
        String expectedMessage = "Updated preorder details for Alice Pauline: "
                + "Date: 31 Dec 2099 | Method: DELIVERY | Handover notes: Call on arrival";

        assertCommandSuccess(new PreorderCommand(INDEX_FIRST_PERSON, descriptor), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_dateOnly_keepsItemsAndOtherDetails() {
        assertPreorderUpdated(INDEX_BENSON, BENSON_WITH_PREORDER,
                new EditFulfilmentDescriptorBuilder().withDate("2098-01-15").build(),
                new PreorderBuilder(BENSON_PREORDER).withDate("2098-01-15").build());
    }

    @Test
    public void execute_methodOnly_keepsItemsAndOtherDetails() {
        assertPreorderUpdated(INDEX_BENSON, BENSON_WITH_PREORDER,
                new EditFulfilmentDescriptorBuilder().withMethod(FulfilmentMethod.DELIVERY).build(),
                new PreorderBuilder(BENSON_PREORDER).withMethod(FulfilmentMethod.DELIVERY).build());
    }

    @Test
    public void execute_handoverNotesOnly_keepsItemsAndOtherDetails() {
        assertPreorderUpdated(INDEX_BENSON, BENSON_WITH_PREORDER,
                new EditFulfilmentDescriptorBuilder().withHandoverNotes("Call on arrival").build(),
                new PreorderBuilder(BENSON_PREORDER).withHandoverNotes("Call on arrival").build());
    }

    @Test
    public void execute_clearHandoverNotes_keepsOtherDetails() {
        Preorder expectedPreorder = new PreorderBuilder().withItems(PINEAPPLE_TARTS, ALMOND_COOKIES)
                .withDate(FUTURE_DATE).withMethod(FulfilmentMethod.COLLECTION).build();

        assertPreorderUpdated(INDEX_BENSON, BENSON_WITH_PREORDER,
                new EditFulfilmentDescriptorBuilder().withoutHandoverNotes().build(), expectedPreorder);
    }

    @Test
    public void execute_clearAllDetails_showsUnsetDetails() {
        EditFulfilmentDescriptor descriptor = new EditFulfilmentDescriptorBuilder().withoutDate().withoutMethod()
                .withoutHandoverNotes().build();
        Preorder expectedPreorder = new PreorderBuilder().withItems(PINEAPPLE_TARTS, ALMOND_COOKIES).build();

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(BENSON_WITH_PREORDER,
                new PersonBuilder(BENSON_WITH_PREORDER).withPreorder(expectedPreorder).build());
        String expectedMessage = "Updated preorder details for Benson Meier: "
                + "Date: Not scheduled | Method: Not set | Handover notes: None";

        assertCommandSuccess(new PreorderCommand(INDEX_BENSON, descriptor), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_onlyClearingWithoutPreorder_noChange() {
        EditFulfilmentDescriptor descriptor = new EditFulfilmentDescriptorBuilder().withoutDate().build();

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        String expectedMessage = String.format(PreorderCommand.MESSAGE_NOTHING_TO_CLEAR, ALICE.getName());

        assertCommandSuccess(new PreorderCommand(INDEX_FIRST_PERSON, descriptor), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_pastDate_showsNote() {
        EditFulfilmentDescriptor descriptor = new EditFulfilmentDescriptorBuilder().withDate(PAST_DATE).build();
        Preorder expectedPreorder = new PreorderBuilder().withDate(PAST_DATE).build();

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(ALICE, new PersonBuilder(ALICE).withPreorder(expectedPreorder).build());
        String expectedMessage = String.format(PreorderCommand.MESSAGE_SUCCESS, ALICE.getName(),
                Messages.formatFulfilmentDetails(expectedPreorder)) + "\nNote: 1 Jan 2020 is in the past.";

        assertCommandSuccess(new PreorderCommand(INDEX_FIRST_PERSON, descriptor), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_futureDate_showsNoNote() {
        assertPreorderUpdated(INDEX_FIRST_PERSON, ALICE,
                new EditFulfilmentDescriptorBuilder().withDate(FUTURE_DATE).build(),
                new PreorderBuilder().withDate(FUTURE_DATE).build());
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_BENSON);
        EditFulfilmentDescriptor descriptor = new EditFulfilmentDescriptorBuilder()
                .withMethod(FulfilmentMethod.DELIVERY).build();
        Preorder expectedPreorder = new PreorderBuilder(BENSON_PREORDER).withMethod(FulfilmentMethod.DELIVERY)
                .build();

        // the filter is kept, so the expected model still shows only Benson
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(BENSON_WITH_PREORDER,
                new PersonBuilder(BENSON_WITH_PREORDER).withPreorder(expectedPreorder).build());
        showPersonAtIndex(expectedModel, INDEX_BENSON);
        String expectedMessage = String.format(PreorderCommand.MESSAGE_SUCCESS, BENSON.getName(),
                Messages.formatFulfilmentDetails(expectedPreorder));

        assertCommandSuccess(new PreorderCommand(INDEX_FIRST_PERSON, descriptor), model, expectedMessage,
                expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditFulfilmentDescriptor descriptor = new EditFulfilmentDescriptorBuilder().withDate(FUTURE_DATE).build();

        assertCommandFailure(new PreorderCommand(outOfBoundIndex, descriptor), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showPersonAtIndex(model, INDEX_BENSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());
        EditFulfilmentDescriptor descriptor = new EditFulfilmentDescriptorBuilder().withDate(FUTURE_DATE).build();

        assertCommandFailure(new PreorderCommand(outOfBoundIndex, descriptor), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        EditFulfilmentDescriptor dateDescriptor = new EditFulfilmentDescriptorBuilder().withDate(FUTURE_DATE)
                .build();
        PreorderCommand preorderFirstCommand = new PreorderCommand(INDEX_FIRST_PERSON, dateDescriptor);

        // same object -> returns true
        assertTrue(preorderFirstCommand.equals(preorderFirstCommand));

        // same values -> returns true
        EditFulfilmentDescriptor dateDescriptorCopy = new EditFulfilmentDescriptorBuilder().withDate(FUTURE_DATE)
                .build();
        assertTrue(preorderFirstCommand.equals(new PreorderCommand(INDEX_FIRST_PERSON, dateDescriptorCopy)));

        // different types -> returns false
        assertFalse(preorderFirstCommand.equals(1));

        // null -> returns false
        assertFalse(preorderFirstCommand.equals(null));

        // different index -> returns false
        assertFalse(preorderFirstCommand.equals(new PreorderCommand(INDEX_SECOND_PERSON, dateDescriptor)));

        // different descriptor -> returns false
        EditFulfilmentDescriptor clearDateDescriptor = new EditFulfilmentDescriptorBuilder().withoutDate().build();
        assertFalse(preorderFirstCommand.equals(new PreorderCommand(INDEX_FIRST_PERSON, clearDateDescriptor)));
    }

    @Test
    public void toStringMethod() {
        EditFulfilmentDescriptor descriptor = new EditFulfilmentDescriptorBuilder().withDate(FUTURE_DATE).build();
        PreorderCommand preorderCommand = new PreorderCommand(INDEX_FIRST_PERSON, descriptor);
        String expected = PreorderCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", editFulfilmentDescriptor=" + descriptor + "}";
        assertEquals(expected, preorderCommand.toString());
    }
}
