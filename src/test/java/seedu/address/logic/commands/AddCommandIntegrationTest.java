package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_MARY_JANE_WITH_EXTRA_SPACES;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.POSTAL_CODE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_MARY_JANE;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.model.search.TextContainsCriterion.Field.EMAIL;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddCommandParser;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_matchesCombinedSearch_preservesCriteriaAndShowsPerson() {
        SearchCriteria criteria = new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, "Alice"), new TextContainsCriterion(EMAIL, "example.com")));
        Person person = new PersonBuilder().withName("Alice Wong").withEmail("alicewong@example.com").build();

        assertAddPreservesSearch(person, criteria, List.of(ALICE, person));
    }

    @Test
    public void execute_matchesOnlySomeCriteria_preservesCriteriaAndKeepsPersonHidden() {
        SearchCriteria criteria = new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, "Alice"), new TextContainsCriterion(EMAIL, "example.com")));
        Person person = new PersonBuilder().withName("Alice Wong").withEmail("alicewong@other.com").build();

        assertAddPreservesSearch(person, criteria, List.of(ALICE));
    }

    @Test
    public void execute_noSearchResults_matchingPersonAppears() {
        SearchCriteria criteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Zoe")));
        Person person = new PersonBuilder().withName("Zoe Tan").build();

        assertAddPreservesSearch(person, criteria, List.of(person));
    }

    @Test
    public void execute_newNameWithPunctuationAndExtraSpaces_success() throws Exception {
        Person expectedPerson = new PersonBuilder(BOB).withPostalCode("654321")
                .withName(VALID_NAME_MARY_JANE).withTags().build();
        AddCommand command = new AddCommandParser().parse(NAME_DESC_MARY_JANE_WITH_EXTRA_SPACES
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(expectedPerson);

        assertCommandSuccess(command, model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(expectedPerson)), expectedModel);
        assertEquals(VALID_NAME_MARY_JANE, model.getAddressBook().getPersonList().getLast().getName().fullName);
    }

    @Test
    public void execute_nameDiffersOnlyInCaseOrSpaces_failure() throws Exception {
        for (String nameVariant : new String[]{"alice pauline", "  Alice   Pauline  ", "  aLiCe   pAuLiNe  "}) {
            AddCommand command = new AddCommandParser().parse(" " + PREFIX_NAME + nameVariant
                    + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB);

            String expectedMessage = String.format(Messages.MESSAGE_DUPLICATE_CUSTOMER, new Name(nameVariant));
            assertCommandFailure(command, model, expectedMessage);
        }
    }

    @Test
    public void execute_nameDuplicatesHiddenPerson_failure() throws Exception {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        AddCommand command = new AddCommandParser().parse(" " + PREFIX_NAME + "  ALICE   PAULINE  "
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB);

        String expectedMessage = String.format(Messages.MESSAGE_DUPLICATE_CUSTOMER, "ALICE PAULINE");
        assertCommandFailure(command, model, expectedMessage);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                String.format(Messages.MESSAGE_DUPLICATE_CUSTOMER, personInList.getName()));
    }

    @Test
    public void execute_duplicateName_addAndEditShowSameMessage() throws Exception {
        Model sampleModel = new ModelManager(SampleDataUtil.getSampleAddressBook(), new UserPrefs());
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("list").execute(sampleModel);

        Command addCommand = parser.parseCommand(
                "add n/alex yeoh p/91234567 e/alex2@example.com a/1 Test Road c/123456");
        Command editCommand = parser.parseCommand("edit 2 n/alex yeoh");
        String expectedMessage = "A customer named \"alex yeoh\" already exists. "
                + "Customer names must be unique (ignoring capitalisation and extra spaces). Choose a different name.";

        assertCommandFailure(addCommand, sampleModel, expectedMessage);
        assertCommandFailure(editCommand, sampleModel, expectedMessage);
    }

    private void assertAddPreservesSearch(Person person, SearchCriteria criteria, List<Person> expectedPersons) {
        model.updateFilteredPersonList(criteria);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(criteria);
        expectedModel.addPerson(person);

        assertCommandSuccess(new AddCommand(person), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(person)), expectedModel);
        assertEquals(criteria, model.getActiveSearchCriteria());
        assertEquals(expectedPersons, model.getFilteredPersonList());
        assertTrue(model.getAddressBook().getPersonList().contains(person));
    }

}
