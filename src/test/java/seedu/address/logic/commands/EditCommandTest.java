package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_MARY_JANE_WITH_EXTRA_SPACES;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_MARY_JANE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.EditCommandParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.search.SearchCriteria;
import seedu.address.testutil.EditPersonDescriptorBuilder;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for EditCommand.
 */
public class EditCommandTest {

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person newDetails = new PersonBuilder().build();
        Person editedPerson = new Person(newDetails.getName(), newDetails.getPhone(), newDetails.getEmail(),
                newDetails.getAddress(), newDetails.getPostalCode(), personToEdit.getTags());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(editedPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_personWithPreorder_preorderKept() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithPreorder = new PersonBuilder(firstPerson)
                .withPostalCode("012345").withPreorder("2 chocolate cakes", ItemStatus.PREPARED).build();
        model.setPerson(firstPerson, personWithPreorder);

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, descriptor);

        Person editedPerson = new PersonBuilder(personWithPreorder).withName(VALID_NAME_BOB).build();
        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personWithPreorder, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        assertEquals(personWithPreorder.getPreorder(), model.getFilteredPersonList().get(0).getPreorder());
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() {
        Index indexLastPerson = Index.fromOneBased(model.getFilteredPersonList().size());
        Person lastPerson = model.getFilteredPersonList().get(indexLastPerson.getZeroBased());

        PersonBuilder personInList = new PersonBuilder(lastPerson);
        Person editedPerson = personInList.withName(VALID_NAME_BOB).withPhone(VALID_PHONE_BOB).build();

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB)
                .withPhone(VALID_PHONE_BOB).build();
        EditCommand editCommand = new EditCommand(indexLastPerson, descriptor);

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(lastPerson, editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editingFields_preservesLeadingZeroPostalCode() throws CommandException {
        Person original = new PersonBuilder().withName("Postal Customer")
                .withPostalCode("012345").withTags("old").build();
        AddressBook postalAddressBook = new AddressBook();
        postalAddressBook.addPerson(original);
        Model postalModel = new ModelManager(postalAddressBook, new UserPrefs());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withName("Updated Customer").build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(postalModel);

        Person edited = postalModel.getAddressBook().getPersonList().get(0);
        assertEquals("012345", edited.getPostalCode().value);
        assertEquals("Updated Customer", edited.getName().fullName);
        assertEquals(1, edited.getTags().size());
        assertTrue(edited.getTags().stream().anyMatch(tag -> tag.tagName.equals("old")));
    }

    @Test
    public void execute_editingContactDetails_preservesPostalCodeTagsAndPreorder() throws Exception {
        Person original = new PersonBuilder().withPostalCode("000001").withTags("regular")
                .withPreorder("Chocolate cake", ItemStatus.PREPARED).build();
        Model postalModel = new ModelManager();
        postalModel.addPerson(original);

        EditCommand command = new EditCommandParser().parse("1 p/98765432 e/new@example.com a/456 New Road");
        Person expected = new PersonBuilder(original).withPhone("98765432").withEmail("new@example.com")
                .withAddress("456 New Road").build();
        command.execute(postalModel);

        Person edited = postalModel.getAddressBook().getPersonList().getFirst();
        assertEquals(expected, edited);
        assertEquals("000001", edited.getPostalCode().value);
        assertEquals(original.getTags(), edited.getTags());
        assertEquals(original.getPreorder(), edited.getPreorder());
    }

    @Test
    public void execute_editingPostalCode_preservesTagsAndPreorder() throws CommandException {
        Person original = new PersonBuilder().withName("Postal Customer")
                .withPostalCode("012345").withTags("old")
                .withPreorder("Chocolate cake", ItemStatus.PENDING).build();
        AddressBook postalAddressBook = new AddressBook();
        postalAddressBook.addPerson(original);
        Model postalModel = new ModelManager(postalAddressBook, new UserPrefs());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPostalCode("654321").build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(postalModel);

        Person edited = postalModel.getAddressBook().getPersonList().get(0);
        assertEquals("654321", edited.getPostalCode().value);
        assertEquals(original.getTags(), edited.getTags());
        assertEquals(original.getPreorder(), edited.getPreorder());
    }

    @Test
    public void execute_editingCustomerWithoutPostalChange_preservesPostalCode() throws CommandException {
        Person original = new PersonBuilder().withName("Existing Customer").withPostalCode("012345").build();
        AddressBook postalAddressBook = new AddressBook();
        postalAddressBook.addPerson(original);
        Model postalModel = new ModelManager(postalAddressBook, new UserPrefs());

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName("Updated Customer").build();
        new EditCommand(INDEX_FIRST_PERSON, descriptor).execute(postalModel);

        assertEquals(original.getPostalCode(), postalModel.getAddressBook().getPersonList().get(0).getPostalCode());
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON, new EditPersonDescriptor());
        Person editedPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_editedPersonNoLongerMatches_preservesCriteria() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        SearchCriteria criteria = model.getActiveSearchCriteria();

        Person personInFilteredList = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personInFilteredList).withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        String expectedMessage = String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.updateFilteredPersonList(criteria);
        expectedModel.setPerson(model.getFilteredPersonList().get(0), editedPerson);

        assertCommandSuccess(editCommand, model, expectedMessage, expectedModel);
        assertEquals(criteria, model.getActiveSearchCriteria());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void execute_nameWithPunctuationAndExtraSpaces_success() throws Exception {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withName(VALID_NAME_MARY_JANE).build();
        EditCommand command = new EditCommandParser().parse("1" + NAME_DESC_MARY_JANE_WITH_EXTRA_SPACES);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)), expectedModel);
        assertEquals(VALID_NAME_MARY_JANE, model.getAddressBook().getPersonList().getFirst().getName().fullName);
    }

    @Test
    public void execute_unchangedNameWithOtherChanges_success() throws Exception {
        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withPhone(VALID_PHONE_BOB).build();
        EditCommand command = new EditCommandParser().parse("1 " + PREFIX_NAME + personToEdit.getName().fullName
                + PHONE_DESC_BOB);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)), expectedModel);
    }

    @Test
    public void execute_ownNameWithCaseAndSpacingChangesFilteredList_success() throws Exception {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        SearchCriteria criteria = model.getActiveSearchCriteria();

        Person personToEdit = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person editedPerson = new PersonBuilder(personToEdit).withName("aLiCe PaUlInE").build();
        EditCommand command = new EditCommandParser().parse("1 " + PREFIX_NAME + "  aLiCe   PaUlInE  ");

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(criteria);
        expectedModel.setPerson(personToEdit, editedPerson);

        assertCommandSuccess(command, model,
                String.format(EditCommand.MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson)), expectedModel);
        assertEquals("aLiCe PaUlInE", model.getAddressBook().getPersonList().getFirst().getName().fullName);
        assertEquals(criteria, model.getActiveSearchCriteria());
        assertEquals(List.of(editedPerson), model.getFilteredPersonList());
    }

    @Test
    public void execute_nameDuplicatesAnotherPersonUnfilteredList_failure() throws Exception {
        for (String nameVariant : new String[]{"alice pauline", "  Alice   Pauline  ", "  aLiCe   pAuLiNe  "}) {
            EditCommand command = new EditCommandParser().parse("2 " + PREFIX_NAME + nameVariant + PHONE_DESC_BOB);

            assertCommandFailure(command, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
        }
    }

    @Test
    public void execute_nameDuplicatesHiddenPersonFilteredList_failure() throws Exception {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        for (String nameVariant : new String[]{"benson meier", "  Benson   Meier  ", "  bEnSoN   mEiEr  "}) {
            EditCommand command = new EditCommandParser().parse("1 " + PREFIX_NAME + nameVariant);

            assertCommandFailure(command, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
        }
    }

    @Test
    public void execute_duplicatePersonUnfilteredList_failure() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder(firstPerson).build();
        EditCommand editCommand = new EditCommand(INDEX_SECOND_PERSON, descriptor);

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePersonFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        // edit person in filtered list into a duplicate in address book
        Person personInList = model.getAddressBook().getPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        EditCommand editCommand = new EditCommand(INDEX_FIRST_PERSON,
                new EditPersonDescriptorBuilder(personInList).build());

        assertCommandFailure(editCommand, model, EditCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_invalidPersonIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build();
        EditCommand editCommand = new EditCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidPersonIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);
        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        EditCommand editCommand = new EditCommand(outOfBoundIndex,
                new EditPersonDescriptorBuilder().withName(VALID_NAME_BOB).build());

        assertCommandFailure(editCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        final EditCommand standardCommand = new EditCommand(INDEX_FIRST_PERSON, DESC_AMY);

        // same values -> returns true
        EditPersonDescriptor copyDescriptor = new EditPersonDescriptor(DESC_AMY);
        EditCommand commandWithSameValues = new EditCommand(INDEX_FIRST_PERSON, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_SECOND_PERSON, DESC_AMY)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditCommand(INDEX_FIRST_PERSON, DESC_BOB)));
    }

    @Test
    public void toStringMethod() {
        Index index = Index.fromOneBased(1);
        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();
        EditCommand editCommand = new EditCommand(index, editPersonDescriptor);
        String expected = EditCommand.class.getCanonicalName() + "{index=" + index + ", editPersonDescriptor="
                + editPersonDescriptor + "}";
        assertEquals(expected, editCommand.toString());
    }

}
