package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.model.preorder.PreorderStatus.COMPLETED;
import static seedu.address.model.preorder.PreorderStatus.PENDING;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.OrderCommand.EditPreorderDescriptor;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.PreorderDetails;
import seedu.address.model.preorder.PreorderStatus;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class OrderCommandTest {
    private static final Person ORDERED_ALICE = new PersonBuilder(ALICE).withPreorder("Cake", PENDING).build();

    @Test
    public void execute_newPreorder_addsBothFields() throws Exception {
        Model model = createModel(ALICE);
        Person expected = new PersonBuilder(ALICE).withPreorder("Tarts", COMPLETED).build();
        assertEquals(new CommandResult(String.format(OrderCommand.MESSAGE_ADD_ORDER_SUCCESS,
                Messages.format(expected))),
                command(1, "Tarts", COMPLETED).execute(model));
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_newPreorderMissingFields_failureWithoutMutation() {
        assertCommandFailure(command(1, null, null), createModel(ALICE), OrderCommand.MESSAGE_NO_ORDER_YET);
        assertCommandFailure(command(1, "Tarts", null), createModel(ALICE), OrderCommand.MESSAGE_NO_ORDER_YET);
        assertCommandFailure(command(1, null, COMPLETED), createModel(ALICE), OrderCommand.MESSAGE_NO_ORDER_YET);
    }

    @Test
    public void execute_existingPreorder_updatesOnlySuppliedFields() throws Exception {
        assertUpdate("Tarts", null, "Tarts", PENDING);
        assertUpdate(null, COMPLETED, "Cake", COMPLETED);
        assertUpdate("Tarts", COMPLETED, "Tarts", COMPLETED);
    }

    @Test
    public void execute_noFields_preservesPreorderAndFilter() throws Exception {
        Model model = createModel(BENSON, ORDERED_ALICE);
        model.updateFilteredPersonList(person -> person.equals(ORDERED_ALICE));
        AddressBook before = new AddressBook(model.getAddressBook());
        assertEquals(new CommandResult(OrderCommand.MESSAGE_NOT_EDITED), command(1, null, null).execute(model));
        assertEquals(before, model.getAddressBook());
        assertEquals(List.of(ORDERED_ALICE), model.getFilteredPersonList());
    }

    @Test
    public void execute_filteredList_updatesDisplayedPersonAndPreservesFilter() throws Exception {
        Model model = createModel(BENSON, ORDERED_ALICE);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        command(1, null, COMPLETED).execute(model);
        Person expected = new PersonBuilder(ALICE).withPreorder("Cake", COMPLETED).build();
        assertEquals(List.of(BENSON, expected), model.getAddressBook().getPersonList());
        assertEquals(List.of(expected), model.getFilteredPersonList());
    }

    @Test
    public void execute_newPreorder_preservesNameFilter() throws Exception {
        Model model = createModel(BENSON, ALICE);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of("Alice")));
        command(1, "Cake", PENDING).execute(model);
        assertEquals(List.of(ORDERED_ALICE), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, ORDERED_ALICE), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_statusChange_removesOrderFromPendingView() throws Exception {
        Person orderedBenson = new PersonBuilder(BENSON).withPreorder("Tarts", PENDING).build();
        Model model = createModel(ORDERED_ALICE, orderedBenson);
        new TrackCommand(Optional.of(PENDING)).execute(model);
        command(1, null, COMPLETED).execute(model);
        Person completedAlice = new PersonBuilder(ALICE).withPreorder("Cake", COMPLETED).build();
        assertEquals(List.of(orderedBenson), model.getFilteredPersonList());
        assertEquals(List.of(completedAlice, orderedBenson), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_detailsChange_preservesStatusFilter() throws Exception {
        Model model = createModel(BENSON, ORDERED_ALICE);
        new TrackCommand(Optional.of(PENDING)).execute(model);
        command(1, "Cookies", null).execute(model);
        Person expected = new PersonBuilder(ALICE).withPreorder("Cookies", PENDING).build();
        assertEquals(List.of(expected), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, expected), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_completeLastActiveOrder_leavesEmptyFilteredView() throws Exception {
        Model model = createModel(BENSON, ORDERED_ALICE);
        new TrackCommand(Optional.empty()).execute(model);
        command(1, null, PreorderStatus.PREPARED).execute(model);
        assertEquals(List.of(new PersonBuilder(ALICE).withPreorder("Cake", PreorderStatus.PREPARED).build()),
                model.getFilteredPersonList());
        command(1, null, COMPLETED).execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(BENSON, new PersonBuilder(ALICE).withPreorder("Cake", COMPLETED).build()),
                model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_invalidDisplayedIndex_failureWithoutMutation() {
        Model model = createModel(BENSON, ORDERED_ALICE);
        model.updateFilteredPersonList(person -> person.equals(ORDERED_ALICE));
        assertCommandFailure(command(2, "Tarts", COMPLETED), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        assertCommandFailure(command(1, "Tarts", COMPLETED), createModel(),
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void constructor_descriptorChangedLater_preservesOriginalChanges() throws Exception {
        EditPreorderDescriptor descriptor = new EditPreorderDescriptor();
        descriptor.setDetails(new PreorderDetails("Tarts"));
        OrderCommand command = new OrderCommand(Index.fromOneBased(1), descriptor);
        descriptor.setDetails(new PreorderDetails("Cookies"));
        descriptor.setStatus(COMPLETED);
        Model model = createModel(ORDERED_ALICE);
        command.execute(model);
        assertEquals(new PersonBuilder(ALICE).withPreorder("Tarts", PENDING).build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void equalsAndHashCode_compareIndexAndSuppliedFields() {
        OrderCommand command = command(1, "Cake", PENDING);
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
    public void toString_includesIndexAndDescriptor() {
        String prefix = OrderCommand.class.getCanonicalName() + "{index="
                + Index.class.getCanonicalName() + "{zeroBasedIndex=0}, editPreorderDescriptor="
                + EditPreorderDescriptor.class.getCanonicalName();
        assertEquals(prefix + "{details=Cake, status=PENDING}}", command(1, "Cake", PENDING).toString());
        assertEquals(prefix + "{details=null, status=null}}", command(1, null, null).toString());
        assertEquals(prefix + "{details=Cake, status=null}}", command(1, "Cake", null).toString());
        assertEquals(prefix + "{details=null, status=PENDING}}", command(1, null, PENDING).toString());
    }

    @Test
    public void descriptorEquals_comparesSuppliedFields() {
        EditPreorderDescriptor descriptor = new EditPreorderDescriptor();
        descriptor.setDetails(new PreorderDetails("Cake"));
        descriptor.setStatus(PENDING);
        EditPreorderDescriptor copy = new EditPreorderDescriptor(descriptor);

        assertTrue(descriptor.equals(descriptor));
        assertTrue(descriptor.equals(copy));
        assertTrue(copy.equals(descriptor));
        assertFalse(descriptor.equals(null));
        assertFalse(descriptor.equals("order"));
        assertFalse(descriptor.equals(new EditPreorderDescriptor()));
        assertEquals(new EditPreorderDescriptor(), new EditPreorderDescriptor());

        copy.setDetails(new PreorderDetails("Tarts"));
        assertFalse(descriptor.equals(copy));
        copy.setDetails(new PreorderDetails("Cake"));
        copy.setStatus(COMPLETED);
        assertFalse(descriptor.equals(copy));
        copy.setStatus(null);
        assertFalse(descriptor.equals(copy));
    }

    @Test
    public void descriptorToString_includesSuppliedAndOmittedFields() {
        EditPreorderDescriptor descriptor = new EditPreorderDescriptor();
        String prefix = EditPreorderDescriptor.class.getCanonicalName();
        assertEquals(prefix + "{details=null, status=null}", descriptor.toString());
        descriptor.setDetails(new PreorderDetails("Cake"));
        assertEquals(prefix + "{details=Cake, status=null}", descriptor.toString());
        descriptor.setStatus(PENDING);
        assertEquals(prefix + "{details=Cake, status=PENDING}", descriptor.toString());
        descriptor.setDetails(null);
        assertEquals(prefix + "{details=null, status=PENDING}", descriptor.toString());
    }

    private void assertUpdate(String details, PreorderStatus status, String expectedDetails,
            PreorderStatus expectedStatus) throws Exception {
        Model model = createModel(ORDERED_ALICE);
        Person expected = new PersonBuilder(ALICE).withPreorder(expectedDetails, expectedStatus).build();
        assertEquals(new CommandResult(String.format(OrderCommand.MESSAGE_EDIT_ORDER_SUCCESS,
                Messages.format(expected))), command(1, details, status).execute(model));
        assertEquals(List.of(expected), model.getAddressBook().getPersonList());
    }

    private OrderCommand command(int index, String details, PreorderStatus status) {
        EditPreorderDescriptor descriptor = new EditPreorderDescriptor();
        if (details != null) {
            descriptor.setDetails(new PreorderDetails(details));
        }
        descriptor.setStatus(status);
        return new OrderCommand(Index.fromOneBased(index), descriptor);
    }

    private Model createModel(Person... persons) {
        AddressBookBuilder builder = new AddressBookBuilder();
        for (Person person : persons) {
            builder.withPerson(person);
        }
        return new ModelManager(builder.build(), new UserPrefs());
    }
}
