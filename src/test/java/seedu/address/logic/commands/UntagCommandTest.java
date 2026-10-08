package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.PreorderStatus;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests tag removal and its interaction with the model.
 */
public class UntagCommandTest {

    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_lastTag_success() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person expectedPerson = new PersonBuilder(original).withTags().build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, expectedPerson);

        assertCommandSuccess(new UntagCommand(INDEX_FIRST_PERSON, new Tag("friends")), model,
                "Removed tag(s) [friends] from " + original.getName(), expectedModel);
    }

    @Test
    public void execute_personWithPreorder_preorderKept() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person withPreorder = new PersonBuilder(original)
                .withPreorder("2 chocolate cakes", PreorderStatus.PREPARED).build();
        model.setPerson(original, withPreorder);

        Person expectedPerson = new PersonBuilder(withPreorder).withTags().build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(withPreorder, expectedPerson);

        assertCommandSuccess(new UntagCommand(INDEX_FIRST_PERSON, new Tag("friends")), model,
                "Removed tag(s) [friends] from " + original.getName(), expectedModel);
        assertEquals(withPreorder.getPreorder(), model.getFilteredPersonList().get(0).getPreorder());
    }

    @Test
    public void execute_differentCaseFilteredList_preservesOtherTagsAndFilter() {
        Person original = model.getFilteredPersonList().get(INDEX_SECOND_PERSON.getZeroBased());
        Person expectedPerson = new PersonBuilder(original).withTags("owesMoney").build();
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, expectedPerson);
        showPersonAtIndex(expectedModel, INDEX_SECOND_PERSON);
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        assertCommandSuccess(new UntagCommand(INDEX_FIRST_PERSON, new Tag("FRIENDS")), model,
                "Removed tag(s) [FRIENDS] from " + original.getName(), expectedModel);
    }

    @Test
    public void execute_multipleCaseVariants_removesAllMatches() {
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person taggedPerson = new PersonBuilder(original).withTags("VIP", "vip", "regular").build();
        model.setPerson(original, taggedPerson);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(taggedPerson, new PersonBuilder(taggedPerson).withTags("regular").build());

        assertCommandSuccess(new UntagCommand(INDEX_FIRST_PERSON, new Tag("Vip")), model,
                "Removed tag(s) [Vip] from " + original.getName(), expectedModel);
    }

    @Test
    public void execute_missingTagFilteredList_failureWithoutChanges() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);
        Person original = model.getFilteredPersonList().getFirst();

        assertCommandFailure(new UntagCommand(INDEX_FIRST_PERSON, new Tag("friend")), model,
                original.getName() + " does not have tag [friend]");
    }

    @Test
    public void execute_personWithoutTags_failureWithoutChanges() {
        Person original = model.getFilteredPersonList().getFirst();
        model.setPerson(original, new PersonBuilder(original).withTags().build());

        assertCommandFailure(new UntagCommand(INDEX_FIRST_PERSON, new Tag("friends")), model,
                original.getName() + " does not have tag [friends]");
    }

    @Test
    public void execute_invalidIndexUnfilteredList_failureWithoutChanges() {
        int listSize = model.getFilteredPersonList().size();
        UntagCommand command = new UntagCommand(Index.fromOneBased(listSize + 1), new Tag("friends"));

        assertCommandFailure(command, model, Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_invalidIndexFilteredList_failureWithoutChanges() {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        assertCommandFailure(new UntagCommand(INDEX_SECOND_PERSON, new Tag("friends")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyDisplayedList_failureWithoutChanges() {
        model.updateFilteredPersonList(person -> false);

        assertCommandFailure(new UntagCommand(INDEX_FIRST_PERSON, new Tag("friends")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void equals() {
        UntagCommand command = new UntagCommand(INDEX_FIRST_PERSON, new Tag("friends"));

        // same object -> returns true
        assertTrue(command.equals(command));

        // same values -> returns true
        assertTrue(command.equals(new UntagCommand(INDEX_FIRST_PERSON, new Tag("friends"))));

        // different types -> returns false
        assertFalse(command.equals(1));

        // null -> returns false
        assertFalse(command.equals(null));

        // different index -> returns false
        assertFalse(command.equals(new UntagCommand(INDEX_SECOND_PERSON, new Tag("friends"))));

        // different tag -> returns false
        assertFalse(command.equals(new UntagCommand(INDEX_FIRST_PERSON, new Tag("VIP"))));
    }

    @Test
    public void toStringMethod() {
        Tag tag = new Tag("friends");
        UntagCommand command = new UntagCommand(INDEX_FIRST_PERSON, tag);
        String expected = UntagCommand.class.getCanonicalName() + "{index=" + INDEX_FIRST_PERSON
                + ", tag=" + tag + "}";

        assertEquals(expected, command.toString());
    }
}
