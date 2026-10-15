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
import seedu.address.model.preorder.PreorderStatus;
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

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToTag, taggedPerson);

        assertCommandSuccess(tagCommand, model, getSuccessMessage(personToTag), expectedModel);
    }

    @Test
    public void execute_personWithPreorder_preorderKept() {
        Person firstPerson = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person personWithPreorder = new PersonBuilder(firstPerson)
                .withPreorder("2 chocolate cakes", PreorderStatus.PENDING).build();
        model.setPerson(firstPerson, personWithPreorder);

        Person taggedPerson = new PersonBuilder(personWithPreorder)
                .withTags(EXISTING_TAG_OF_FIRST_PERSON, VALID_TAG_HUSBAND).build();
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, TAG_HUSBAND);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personWithPreorder, taggedPerson);

        assertCommandSuccess(tagCommand, model, getSuccessMessage(personWithPreorder), expectedModel);
        assertEquals(personWithPreorder.getPreorder(), model.getFilteredPersonList().get(0).getPreorder());
    }

    @Test
    public void execute_validIndexFilteredList_successAndFilterKept() {
        showPersonAtIndex(model, INDEX_FIRST_PERSON);

        Person personToTag = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person taggedPerson = new PersonBuilder(personToTag)
                .withTags(EXISTING_TAG_OF_FIRST_PERSON, VALID_TAG_HUSBAND).build();
        TagCommand tagCommand = new TagCommand(INDEX_FIRST_PERSON, TAG_HUSBAND);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.setPerson(personToTag, taggedPerson);
        showPersonAtIndex(expectedModel, INDEX_FIRST_PERSON);

        assertCommandSuccess(tagCommand, model, getSuccessMessage(personToTag), expectedModel);
    }

    @Test
    public void execute_existingTag_noChange() {
        assertTagAlreadyPresent(EXISTING_TAG_OF_FIRST_PERSON);
    }

    @Test
    public void execute_existingTagDifferentCase_noChange() {
        assertTagAlreadyPresent(EXISTING_TAG_OF_FIRST_PERSON.toUpperCase());
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

    /**
     * Returns the success message for adding {@code TAG_HUSBAND} to {@code personToTag}.
     */
    private static String getSuccessMessage(Person personToTag) {
        return String.format(TagCommand.MESSAGE_TAG_PERSON_SUCCESS, TAG_HUSBAND, personToTag.getName());
    }

    /**
     * Asserts that tagging the first person with {@code tagName}, which they already have,
     * reports the duplicate and leaves the model unchanged.
     */
    private void assertTagAlreadyPresent(String tagName) {
        Person personToTag = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Tag tag = new Tag(tagName);
        String expectedMessage = String.format(TagCommand.MESSAGE_DUPLICATE_TAG, personToTag.getName(), tag);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(new TagCommand(INDEX_FIRST_PERSON, tag), model, expectedMessage, expectedModel);
    }
}
