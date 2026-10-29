package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

/** Tests item addition and its interaction with the model and filtered views. */
public class AddItemCommandTest {
    private static final Index FIRST = Index.fromOneBased(1);
    private static final Item ITEM = new ItemBuilder().withRemark("Less sugar").build();

    @Test
    public void execute_noPreorder_createsPreorder() throws Exception {
        Model model = createModel(ALICE, BENSON);
        CommandResult result = new AddItemCommand(FIRST, ITEM).execute(model);
        assertEquals(new CommandResult(String.format(AddItemCommand.MESSAGE_ADD_ITEM_SUCCESS,
                1, ALICE.getName(), ITEM.getName(), ITEM.getQuantity(), ITEM.getStatus())), result);
        assertEquals(List.of(new PersonBuilder(ALICE).withPreorder(ITEM).build(), BENSON),
                model.getAddressBook().getPersonList());
        assertEquals(Optional.empty(), ALICE.getPreorder());
    }

    @Test
    public void execute_emptyPreorder_addsFirstItem() throws Exception {
        Person original = new PersonBuilder(ALICE).withPreorder().build();
        Model model = createModel(original);
        new AddItemCommand(FIRST, ITEM).execute(model);
        assertEquals(List.of(new PersonBuilder(ALICE).withPreorder(ITEM).build()),
                model.getAddressBook().getPersonList());
        assertEquals(List.of(), original.getPreorder().orElseThrow().getItems());
    }

    @Test
    public void execute_existingPreorder_preservesItemsAndAppendsDuplicate() throws Exception {
        Item completed = new ItemBuilder().withQuantity("5").withStatus(ItemStatus.COMPLETED).build();
        Person original = new PersonBuilder(ALICE).withPreorder(ITEM, completed).build();
        Model model = createModel(original, BENSON);
        CommandResult result = new AddItemCommand(FIRST, ITEM).execute(model);
        assertEquals(new CommandResult("Added item 3 for Alice Pauline: Pineapple Tarts (quantity: 2, PENDING)"),
                result);
        assertEquals(List.of(new PersonBuilder(ALICE).withPreorder(ITEM, completed, ITEM).build(), BENSON),
                model.getAddressBook().getPersonList());
        assertEquals(List.of(ITEM, completed), original.getPreorder().orElseThrow().getItems());
    }

    @Test
    public void execute_filteredList_targetsDisplayedCustomerAndPreservesFilter() throws Exception {
        Model model = createModel(BENSON, ALICE);
        showPersonAtIndex(model, Index.fromOneBased(2));
        new AddItemCommand(FIRST, ITEM).execute(model);
        Person expected = new PersonBuilder(ALICE).withPreorder(ITEM).build();
        assertEquals(List.of(BENSON, expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected), model.getFilteredPersonList());
    }

    @Test
    public void execute_statusFilter_preservesFilterAfterAddingCompletedItem() throws Exception {
        Person original = new PersonBuilder(ALICE).withPreorder(ITEM).build();
        Model model = createModel(original, BENSON);
        new TrackCommand(Optional.of(ItemStatus.PENDING)).execute(model);
        Item completed = new ItemBuilder().withStatus(ItemStatus.COMPLETED).build();
        new AddItemCommand(FIRST, completed).execute(model);
        Person expected = new PersonBuilder(ALICE).withPreorder(ITEM, completed).build();
        assertEquals(List.of(expected), model.getFilteredPersonList());
        assertEquals(List.of(expected, BENSON), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_invalidIndex_failureWithoutMutation() {
        AddItemCommand second = new AddItemCommand(Index.fromOneBased(2), ITEM);
        assertCommandFailure(second, createModel(ALICE), Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(new AddItemCommand(FIRST, ITEM), createModel(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        Model filtered = createModel(BENSON, ALICE);
        showPersonAtIndex(filtered, Index.fromOneBased(2));
        assertCommandFailure(second, filtered, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddItemCommand(null, ITEM));
        assertThrows(NullPointerException.class, () -> new AddItemCommand(FIRST, null));
    }

    @Test
    public void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new AddItemCommand(FIRST, ITEM).execute(null));
    }

    @Test
    public void equalsAndHashCode_compareIndexAndItem() {
        AddItemCommand command = new AddItemCommand(FIRST, ITEM);
        AddItemCommand copy = new AddItemCommand(FIRST, new ItemBuilder(ITEM).build());
        assertEquals(command, command);
        assertEquals(command, copy);
        assertEquals(command.hashCode(), copy.hashCode());
        assertNotEquals(command, new AddItemCommand(Index.fromOneBased(2), ITEM));
        assertNotEquals(command, new AddItemCommand(FIRST, new ItemBuilder(ITEM).withName("Cookies").build()));
        assertNotEquals(command, new AddItemCommand(FIRST, new ItemBuilder(ITEM).withQuantity("3").build()));
        assertNotEquals(command, new AddItemCommand(FIRST, new ItemBuilder(ITEM).withRemark("Gift").build()));
        Item completed = new ItemBuilder(ITEM).withStatus(ItemStatus.COMPLETED).build();
        assertNotEquals(command, new AddItemCommand(FIRST, completed));
        assertNotEquals(command, null);
        assertNotEquals(command, "additem");
    }

    @Test
    public void toString_includesIndexAndItem() {
        String expected = AddItemCommand.class.getCanonicalName() + "{index=" + FIRST + ", item=" + ITEM + "}";
        assertEquals(expected, new AddItemCommand(FIRST, ITEM).toString());
    }

    @Test
    public void execute_newPreorder_preservesPostalCodeAndUnsetFulfilment() throws Exception {
        Person original = new PersonBuilder(ALICE).withPostalCode("012345").build();
        Model model = createModel(original, BENSON);
        new AddItemCommand(FIRST, ITEM).execute(model);
        Person expected = new PersonBuilder(original).withPreorder(ITEM).build();
        assertEquals(List.of(expected, BENSON), model.getAddressBook().getPersonList());
        assertEquals(Optional.empty(), original.getPreorder());
    }

    @Test
    public void execute_existingPreorder_preservesPostalCodeAndFulfilment() throws Exception {
        for (List<Item> existing : List.of(List.<Item>of(), List.of(ITEM))) {
            for (FulfilmentMethod method : FulfilmentMethod.values()) {
                assertFulfilmentPreserved(existing, Optional.of(new FulfilmentDate("2026-12-20")),
                        Optional.of(method), Optional.of(new HandoverNotes("Call on arrival")));
            }
        }
    }

    @Test
    public void execute_partialFulfilment_preservesPresentAndAbsentFields() throws Exception {
        assertFulfilmentPreserved(List.of(), Optional.of(new FulfilmentDate("2026-12-20")),
                Optional.empty(), Optional.empty());
        assertFulfilmentPreserved(List.of(ITEM), Optional.empty(), Optional.of(FulfilmentMethod.COLLECTION),
                Optional.empty());
        assertFulfilmentPreserved(List.of(), Optional.empty(), Optional.empty(),
                Optional.of(new HandoverNotes("Call on arrival")));
    }

    private void assertFulfilmentPreserved(List<Item> existing, Optional<FulfilmentDate> date,
            Optional<FulfilmentMethod> method, Optional<HandoverNotes> notes) throws Exception {
        Preorder preorder = new Preorder(existing, date, method, notes);
        Person original = new PersonBuilder(ALICE).withPostalCode("012345").withPreorder(preorder).build();
        Model model = createModel(BENSON, original);
        showPersonAtIndex(model, Index.fromOneBased(2));
        new AddItemCommand(FIRST, ITEM).execute(model);
        List<Item> expectedItems = new ArrayList<>(existing);
        expectedItems.add(ITEM);
        Person expected = new PersonBuilder(original)
                .withPreorder(new Preorder(expectedItems, date, method, notes)).build();
        assertEquals(List.of(BENSON, expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected), model.getFilteredPersonList());
        assertEquals(preorder, original.getPreorder().orElseThrow());
        assertEquals(existing, preorder.getItems());
    }

    private Model createModel(Person... persons) {
        AddressBookBuilder builder = new AddressBookBuilder();
        for (Person person : persons) {
            builder.withPerson(person);
        }
        return new ModelManager(builder.build(), new UserPrefs());
    }
}
