package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.model.preorder.ItemStatus.COMPLETED;
import static seedu.address.model.preorder.ItemStatus.PENDING;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditItemCommand.EditItemDescriptor;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.preorder.Remark;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

public class EditItemCommandTest {
    private static final Person ORDERED_ALICE = new PersonBuilder(ALICE).withPreorder("Cake", PENDING).build();

    @Test
    public void execute_existingPreorder_updatesOnlySuppliedFields() throws Exception {
        assertUpdate("Tarts", null, "Tarts", PENDING);
        assertUpdate(null, COMPLETED, "Cake", COMPLETED);
        assertUpdate("Tarts", COMPLETED, "Tarts", COMPLETED);
    }

    @Test
    public void execute_noFields_failureWithoutMutation() {
        assertCommandFailure(command(1, null, null), createModel(ORDERED_ALICE), EditItemCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void execute_filteredList_updatesDisplayedPersonAndPreservesFilter() throws Exception {
        Model model = createModel(BENSON, ORDERED_ALICE);
        showPersonAtIndex(model, Index.fromOneBased(2));
        command(1, null, COMPLETED).execute(model);
        Person expected = new PersonBuilder(ALICE).withPreorder("Cake", COMPLETED).build();
        assertEquals(List.of(BENSON, expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected), model.getFilteredPersonList());
    }

    @Test
    public void execute_statusChange_removesCustomerFromPendingView() throws Exception {
        Person orderedBenson = new PersonBuilder(BENSON).withPreorder("Tarts", PENDING).build();
        Model model = createModel(ORDERED_ALICE, orderedBenson);
        new TrackCommand(Optional.of(PENDING)).execute(model);
        command(1, null, COMPLETED).execute(model);
        Person completedAlice = new PersonBuilder(ALICE).withPreorder("Cake", COMPLETED).build();
        assertEquals(List.of(orderedBenson), model.getFilteredPersonList());
        assertEquals(List.of(completedAlice, orderedBenson), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_nameChange_preservesStatusFilter() throws Exception {
        Model model = createModel(BENSON, ORDERED_ALICE);
        new TrackCommand(Optional.of(PENDING)).execute(model);
        command(1, "Cookies", null).execute(model);
        Person expected = new PersonBuilder(ALICE).withPreorder("Cookies", PENDING).build();
        assertEquals(List.of(expected), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, expected), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_completeLastActiveItem_leavesEmptyFilteredView() throws Exception {
        Model model = createModel(BENSON, ORDERED_ALICE);
        new TrackCommand(Optional.empty()).execute(model);
        command(1, null, ItemStatus.PREPARED).execute(model);
        assertEquals(List.of(new PersonBuilder(ALICE).withPreorder("Cake", ItemStatus.PREPARED).build()),
                model.getFilteredPersonList());
        command(1, null, COMPLETED).execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, new PersonBuilder(ALICE).withPreorder("Cake", COMPLETED).build()),
                model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_invalidDisplayedIndex_failureWithoutMutation() {
        Model model = createModel(BENSON, ORDERED_ALICE);
        showPersonAtIndex(model, Index.fromOneBased(2));
        assertCommandFailure(command(2, "Tarts", COMPLETED), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(command(1, "Tarts", COMPLETED), createModel(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_descriptorChangedLater_preservesOriginalChanges() throws Exception {
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setName(new ItemName("Tarts"));
        EditItemCommand command = new EditItemCommand(Index.fromOneBased(1), Index.fromOneBased(1), descriptor);
        descriptor.setName(new ItemName("Cookies"));
        descriptor.setStatus(COMPLETED);
        Model model = createModel(ORDERED_ALICE);
        command.execute(model);
        assertEquals(new PersonBuilder(ALICE).withPreorder("Tarts", PENDING).build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void equalsAndHashCode_compareIndexAndSuppliedFields() {
        EditItemCommand command = command(1, "Cake", PENDING);
        assertTrue(command.equals(command));
        assertEquals(command, command(1, "Cake", PENDING));
        assertEquals(command.hashCode(), command(1, "Cake", PENDING).hashCode());
        assertNotEquals(command, command(2, "Cake", PENDING));
        assertNotEquals(command, command(1, "Tarts", PENDING));
        assertNotEquals(command, command(1, "Cake", COMPLETED));
        assertNotEquals(command, command(1, null, PENDING));
        assertNotEquals(command, command(1, "Cake", null));
        assertEquals(command(1, null, null), command(1, null, null));
        assertNotEquals(command, null);
        assertNotEquals(command, "order");
    }

    @Test
    public void toString_includesIndicesAndDescriptor() {
        String text = command(1, "Cake", PENDING).toString();
        assertTrue(text.contains("index="));
        assertTrue(text.contains("itemIndex="));
        assertTrue(text.contains("editItemDescriptor="));
    }

    @Test
    public void descriptorEquals_comparesSuppliedFields() {
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setName(new ItemName("Cake"));
        descriptor.setStatus(PENDING);
        EditItemDescriptor copy = new EditItemDescriptor(descriptor);

        assertTrue(descriptor.equals(descriptor));
        assertTrue(descriptor.equals(copy));
        assertTrue(copy.equals(descriptor));
        assertFalse(descriptor.equals(null));
        assertFalse(descriptor.equals("order"));
        assertFalse(descriptor.equals(new EditItemDescriptor()));
        assertEquals(new EditItemDescriptor(), new EditItemDescriptor());

        copy.setName(new ItemName("Tarts"));
        assertFalse(descriptor.equals(copy));
        copy.setName(new ItemName("Cake"));
        copy.setStatus(COMPLETED);
        assertFalse(descriptor.equals(copy));
        copy.setStatus(null);
        assertFalse(descriptor.equals(copy));
    }

    @Test
    public void descriptorToString_includesSuppliedAndOmittedFields() {
        EditItemDescriptor descriptor = new EditItemDescriptor();
        String prefix = EditItemDescriptor.class.getCanonicalName();
        assertEquals(prefix + "{name=null, status=null, quantity=null, remark=null}", descriptor.toString());
        descriptor.setName(new ItemName("Cake"));
        assertEquals(prefix + "{name=Cake, status=null, quantity=null, remark=null}", descriptor.toString());
        descriptor.setStatus(PENDING);
        assertEquals(prefix + "{name=Cake, status=PENDING, quantity=null, remark=null}", descriptor.toString());
        descriptor.setName(null);
        assertEquals(prefix + "{name=null, status=PENDING, quantity=null, remark=null}", descriptor.toString());
    }

    @Test
    public void execute_quantityAndRemark_preservesOtherItemsAndCustomerData() throws Exception {
        Item original = new ItemBuilder().withRemark("Less sugar").build();
        Item other = new ItemBuilder().withName("Cookies").build();
        Person person = new PersonBuilder(ALICE).withPreorder(other, original, other).build();
        Model model = createModel(person, BENSON);
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setQuantity(new Quantity("3"));
        descriptor.setRemark(Optional.of(new Remark("Gift box")));
        new EditItemCommand(Index.fromOneBased(1), Index.fromOneBased(2), descriptor).execute(model);
        Item expected = new ItemBuilder(original).withQuantity("3").withRemark("Gift box").build();
        assertEquals(List.of(new PersonBuilder(ALICE).withPreorder(other, expected, other).build(), BENSON),
                model.getAddressBook().getPersonList());
        assertEquals(List.of(other, original, other), person.getPreorder().orElseThrow().getItems());
    }

    @Test
    public void execute_clearRemark_preservesOtherFields() throws Exception {
        Item original = new ItemBuilder().withRemark("Less sugar").build();
        Model model = createModel(new PersonBuilder(ALICE).withPreorder(original).build());
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setRemark(Optional.empty());
        new EditItemCommand(Index.fromOneBased(1), Index.fromOneBased(1), descriptor).execute(model);
        assertEquals(new PersonBuilder(ALICE).withPreorder(new ItemBuilder().build()).build(),
                model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_quantityOmitted_keepsQuantityUntilSupplied() throws Exception {
        Item original = new ItemBuilder().withQuantity("3").withRemark("Less sugar").build();
        Model model = createModel(new PersonBuilder(ALICE).withPreorder(original).build());
        CommandResult result = command(1, null, COMPLETED).execute(model);
        assertEquals(new CommandResult(
                "Edited item 1 for Alice Pauline: Pineapple Tarts (quantity: 3, COMPLETED)"), result);
        Item completed = new ItemBuilder(original).withStatus(COMPLETED).build();
        assertEquals(new PersonBuilder(ALICE).withPreorder(completed).build(),
                model.getFilteredPersonList().getFirst());
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setQuantity(new Quantity("1"));
        CommandResult quantityResult =
                new EditItemCommand(Index.fromOneBased(1), Index.fromOneBased(1), descriptor).execute(model);
        assertEquals(new CommandResult(
                "Edited item 1 for Alice Pauline: Pineapple Tarts (quantity: 1, COMPLETED)"), quantityResult);
        Person expected = new PersonBuilder(ALICE)
                .withPreorder(new ItemBuilder(completed).withQuantity("1").build()).build();
        assertEquals(expected,
                model.getFilteredPersonList().getFirst());
    }

    @Test
    public void execute_missingPreorderOrItem_failureWithoutMutation() {
        assertCommandFailure(command(1, "Cake", null), createModel(ALICE), EditItemCommand.MESSAGE_NO_PREORDER);
        assertCommandFailure(command(1, "Cake", null), createModel(new PersonBuilder(ALICE).withPreorder().build()),
                EditItemCommand.MESSAGE_INVALID_ITEM_INDEX);
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setName(new ItemName("Cake"));
        assertCommandFailure(new EditItemCommand(Index.fromOneBased(1), Index.fromOneBased(2), descriptor),
                createModel(ORDERED_ALICE), EditItemCommand.MESSAGE_INVALID_ITEM_INDEX);
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        Index first = Index.fromOneBased(1);
        EditItemDescriptor descriptor = new EditItemDescriptor();
        assertThrows(NullPointerException.class, () -> new EditItemCommand(null, first, descriptor));
        assertThrows(NullPointerException.class, () -> new EditItemCommand(first, null, descriptor));
        assertThrows(NullPointerException.class, () -> new EditItemCommand(first, first, null));
        assertThrows(NullPointerException.class, () -> command(1, "Cake", null).execute(null));
    }

    @Test
    public void descriptorCopy_preservesQuantityAndRemarkStates() {
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setQuantity(new Quantity("2"));
        descriptor.setRemark(Optional.of(new Remark("Gift")));
        EditItemDescriptor copy = new EditItemDescriptor(descriptor);
        assertEquals(descriptor, copy);
        assertEquals(descriptor.hashCode(), copy.hashCode());
        descriptor.setQuantity(new Quantity("3"));
        descriptor.setRemark(Optional.empty());
        assertEquals(Optional.of(new Quantity("2")), copy.getQuantity());
        assertEquals(Optional.of(Optional.of(new Remark("Gift"))), copy.getRemark());
        assertNotEquals(descriptor, copy);
        assertNotEquals(new EditItemDescriptor(), descriptor);
    }

    private void assertUpdate(String name, ItemStatus status, String expectedName,
            ItemStatus expectedStatus) throws Exception {
        Model model = createModel(ORDERED_ALICE);
        Person expected = new PersonBuilder(ALICE).withPreorder(expectedName, expectedStatus).build();
        assertEquals(new CommandResult(String.format(EditItemCommand.MESSAGE_EDIT_ITEM_SUCCESS,
                1, ALICE.getName(), expectedName, "1", expectedStatus)),
                command(1, name, status).execute(model));
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
    }

    private EditItemCommand command(int index, String name, ItemStatus status) {
        EditItemDescriptor descriptor = new EditItemDescriptor();
        if (name != null) {
            descriptor.setName(new ItemName(name));
        }
        descriptor.setStatus(status);
        return new EditItemCommand(Index.fromOneBased(index), Index.fromOneBased(1), descriptor);
    }

    @Test
    public void execute_editItem_preservesPostalCodeAndFulfilment() throws Exception {
        for (FulfilmentMethod method : FulfilmentMethod.values()) {
            assertFulfilmentPreserved("012345", Optional.of(new FulfilmentDate("2026-12-20")),
                    Optional.of(method), Optional.of(new HandoverNotes("Call on arrival")));
        }
    }

    @Test
    public void execute_partialFulfilment_preservesPostalCodeAndMissingFields() throws Exception {
        assertFulfilmentPreserved("012345", Optional.of(new FulfilmentDate("2026-12-20")),
                Optional.empty(), Optional.empty());
        assertFulfilmentPreserved("012345", Optional.empty(), Optional.of(FulfilmentMethod.COLLECTION),
                Optional.empty());
        assertFulfilmentPreserved("012345", Optional.empty(), Optional.empty(),
                Optional.of(new HandoverNotes("Call on arrival")));
    }

    private void assertFulfilmentPreserved(String postalCode, Optional<FulfilmentDate> date,
            Optional<FulfilmentMethod> method, Optional<HandoverNotes> notes) throws Exception {
        Item first = new ItemBuilder().build();
        Item target = new ItemBuilder().withQuantity("4").withRemark("Less sugar").build();
        Preorder preorder = new Preorder(List.of(first, target), date, method, notes);
        Person original = new PersonBuilder(ALICE).withPostalCode(postalCode).withPreorder(preorder).build();
        Model model = createModel(BENSON, original);
        showPersonAtIndex(model, Index.fromOneBased(2));
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setStatus(COMPLETED);
        descriptor.setRemark(Optional.empty());
        new EditItemCommand(Index.fromOneBased(1), Index.fromOneBased(2), descriptor).execute(model);
        Item updated = new Item(target.getName(), target.getQuantity(), COMPLETED, Optional.empty());
        Person expected = new PersonBuilder(original)
                .withPreorder(new Preorder(List.of(first, updated), date, method, notes)).build();
        assertEquals(List.of(BENSON, expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected), model.getFilteredPersonList());
        assertEquals(preorder, original.getPreorder().orElseThrow());
        assertEquals(List.of(first, target), preorder.getItems());
    }

    private Model createModel(Person... persons) {
        AddressBookBuilder builder = new AddressBookBuilder();
        for (Person person : persons) {
            builder.withPerson(person);
        }
        return new ModelManager(builder.build(), new UserPrefs());
    }
}
