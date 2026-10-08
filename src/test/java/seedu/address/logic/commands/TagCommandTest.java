package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) and unit tests for {@code TagCommand}.
 */
public class TagCommandTest {

    private static final Tag TAG_HUSBAND = new Tag(VALID_TAG_HUSBAND);
    private static final String EXISTING_TAG_OF_FIRST_PERSON = "friends";

    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() {
        Person personToTag = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person taggedPerson = new PersonBuilder(personToTag)
                .withTags(EXISTING_TAG_OF_FIRST_PERSON, VALID_TAG_HUSBAND).build();
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, TAG_HUSBAND);

        String expectedMessage = String.format(TagCommand.MESSAGE_TAG_PERSON_SUCCESS, Messages.format(taggedPerson));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToTag, taggedPerson);

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_validIndexFilteredList_success() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToTag = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person taggedPerson = new PersonBuilder(personToTag)
                .withTags(EXISTING_TAG_OF_FIRST_PERSON, VALID_TAG_HUSBAND).build();
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, TAG_HUSBAND);

        String expectedMessage = String.format(TagCommand.MESSAGE_TAG_PERSON_SUCCESS, Messages.format(taggedPerson));

        // expected model shows all persons, as the command resets the filter
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToTag, taggedPerson);

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_existingTag_success() {
        Person personToTag = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, new Tag(EXISTING_TAG_OF_FIRST_PERSON));

        String expectedMessage = String.format(TagCommand.MESSAGE_TAG_PERSON_SUCCESS, Messages.format(personToTag));

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(tagCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredPersonList().size() + 1);
        TagCommand tagCommand = new TagCommand(outOfBoundIndex, TAG_HUSBAND);

        assertCommandFailure(tagCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_failure() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Index outOfBoundIndex = INDEX_SECOND_PERSON;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getPersonList().size());

        TagCommand tagCommand = new TagCommand(outOfBoundIndex, TAG_HUSBAND);

        assertCommandFailure(tagCommand, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        TagCommand tagFirstCommand = new TagCommand(INDEX_FIRST_PERSON, TAG_HUSBAND);

        // same object -> returns true
        assertTrue(tagFirstCommand.equals(tagFirstCommand));

        // same values -> returns true
        assertTrue(tagFirstCommand.equals(new TagCommand(INDEX_FIRST_PERSON, new Tag(VALID_TAG_HUSBAND))));

        // different types -> returns false
        assertFalse(tagFirstCommand.equals(1));

        // null -> returns false
        assertFalse(tagFirstCommand.equals(null));

        // different index -> returns false
        assertFalse(tagFirstCommand.equals(new TagCommand(INDEX_SECOND_PERSON, TAG_HUSBAND)));

        // different tag -> returns false
        assertFalse(tagFirstCommand.equals(new TagCommand(INDEX_FIRST_PERSON, new Tag(VALID_TAG_FRIEND))));
    }

    @Test
    public void toStringMethod() {
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, TAG_HUSBAND);
        String expected = TagCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", tag=" + TAG_HUSBAND + "}";
        assertEquals(expected, tagCommand.toString());
    }
}
