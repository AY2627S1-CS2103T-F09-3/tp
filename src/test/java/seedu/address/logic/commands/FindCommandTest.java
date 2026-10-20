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
import seedu.address.model.person.Person;
import seedu.address.model.person.PostalCode;
import seedu.address.model.search.PostalCodeCriterion;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TagCriterion;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.PersonBuilder;

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
    public void execute_postalCode_returnsOnlyExactMatches() {
        Person firstMatch = new PersonBuilder(BENSON).withPostalCode("123456").build();
        Person secondMatch = new PersonBuilder(DANIEL).withPostalCode("123456").build();
        Person differentCode = new PersonBuilder(ELLE).withPostalCode("123457").build();
        Person missingCode = new PersonBuilder().withName("Missing Code").build();
        Model searchModel = new ModelManager();
        List.of(firstMatch, differentCode, missingCode, secondMatch).forEach(searchModel::addPerson);
        SearchCriteria criteria = new SearchCriteria(List.of(new PostalCodeCriterion(new PostalCode("123456"))));

        CommandResult result = new FindCommand(criteria).execute(searchModel);

        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 2), result.getFeedbackToUser());
        assertEquals(List.of(firstMatch, secondMatch), searchModel.getFilteredPersonList());
        assertEquals(List.of(firstMatch, differentCode, missingCode, secondMatch),
                searchModel.getAddressBook().getPersonList());
    }

    @Test
    public void execute_postalCodeWithName_requiresBothCriteria() {
        Person match = new PersonBuilder().withName("Alice Tan").withPostalCode("012345").build();
        Person differentName = new PersonBuilder().withName("Bob Tan").withPostalCode("012345").build();
        Person differentCode = new PersonBuilder().withName("Alice Lim").withPostalCode("123456").build();
        Person missingCode = new PersonBuilder().withName("Alice Lee").build();
        Model searchModel = new ModelManager();
        List.of(match, differentName, differentCode, missingCode).forEach(searchModel::addPerson);
        SearchCriteria criteria = new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, "Alice"),
                new PostalCodeCriterion(new PostalCode("012345"))));

        CommandResult result = new FindCommand(criteria).execute(searchModel);

        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1), result.getFeedbackToUser());
        assertEquals(List.of(match), searchModel.getFilteredPersonList());
    }

    @Test
    public void execute_unmatchedPostalCode_returnsEmptyList() {
        Model searchModel = new ModelManager();
        searchModel.addPerson(new PersonBuilder().withPostalCode("012345").build());
        SearchCriteria criteria = new SearchCriteria(List.of(new PostalCodeCriterion(new PostalCode("654321"))));

        CommandResult result = new FindCommand(criteria).execute(searchModel);

        assertEquals(String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 0), result.getFeedbackToUser());
        assertEquals(List.of(), searchModel.getFilteredPersonList());
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
