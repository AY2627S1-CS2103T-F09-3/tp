package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.model.search.TextContainsCriterion.Field.PHONE;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.DANIEL;
import static seedu.address.testutil.TypicalPersons.ELLE;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TagCriterion;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.model.tag.Tag;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void constructor_nullCriteria_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindCommand(null));
    }

    @Test
    public void constructor_emptyCriteria_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new FindCommand(new SearchCriteria(List.of())));
    }

    @Test
    public void equals() {
        SearchCriteria firstCriteria = prepareNameCriteria("first");
        SearchCriteria secondCriteria = prepareNameCriteria("second");

        FindCommand findFirstCommand = new FindCommand(firstCriteria);
        FindCommand findSecondCommand = new FindCommand(secondCriteria);

        // same object -> returns true
        assertTrue(findFirstCommand.equals(findFirstCommand));

        // same values -> returns true
        FindCommand findFirstCommandCopy = new FindCommand(firstCriteria);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        // different types -> returns false
        assertFalse(findFirstCommand.equals(1));

        // null -> returns false
        assertFalse(findFirstCommand.equals(null));

        // different criteria -> returns false
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_noMatchingCustomer_emptyList() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0);
        SearchCriteria criteria = prepareNameCriteria("does-not-exist");
        FindCommand command = new FindCommand(criteria);
        expectedModel.updateFilteredPersonList(criteria);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(), model.getFilteredPersonList());
    }

    @Test
    public void execute_sharedNameFragment_multipleCustomersFound() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2);
        SearchCriteria criteria = prepareNameCriteria("Meier");
        FindCommand command = new FindCommand(criteria);
        expectedModel.updateFilteredPersonList(criteria);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(BENSON, DANIEL), model.getFilteredPersonList());
    }

    @Test
    public void execute_caseInsensitiveSubstring_matchingCustomersFound() {
        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1);
        SearchCriteria criteria = prepareNameCriteria("ELL");
        FindCommand command = new FindCommand(criteria);
        expectedModel.updateFilteredPersonList(criteria);
        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(ELLE), model.getFilteredPersonList());
    }

    @Test
    public void execute_combinedCriteria_requiresEveryCriterion() {
        SearchCriteria criteria = new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, "Meier"),
                new TextContainsCriterion(PHONE, "9876"),
                new TagCriterion(new Tag("FRIENDS"))));
        expectedModel.updateFilteredPersonList(criteria);

        assertCommandSuccess(new FindCommand(criteria), model,
                String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1), expectedModel);
        assertEquals(List.of(BENSON), model.getFilteredPersonList());
    }

    @Test
    public void hashCode_equivalentCriteria_sameHashCode() {
        FindCommand command = new FindCommand(prepareNameCriteria("Alice"));
        assertEquals(command.hashCode(), new FindCommand(prepareNameCriteria("ALICE")).hashCode());
    }

    @Test
    public void toStringMethod() {
        SearchCriteria criteria = prepareNameCriteria("keyword");
        FindCommand findCommand = new FindCommand(criteria);
        String expected = FindCommand.class.getCanonicalName() + "{criteria=" + criteria + "}";
        assertEquals(expected, findCommand.toString());
    }

    /**
     * Creates a name substring search using {@code query}.
     */
    private SearchCriteria prepareNameCriteria(String query) {
        return new SearchCriteria(List.of(new TextContainsCriterion(NAME, query)));
    }
}
