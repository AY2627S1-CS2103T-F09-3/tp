package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_MARY_JANE_WITH_EXTRA_SPACES;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_MARY_JANE;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.logic.commands.CommandTestUtil.showPersonAtIndex;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.BOB;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.parser.AddCommandParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
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
    public void execute_newNameWithPunctuationAndExtraSpaces_success() throws Exception {
        Person expectedPerson = new PersonBuilder(BOB).withName(VALID_NAME_MARY_JANE).withTags().build();
        AddCommand command = new AddCommandParser().parse(NAME_DESC_MARY_JANE_WITH_EXTRA_SPACES
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB);

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
                    + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB);

            assertCommandFailure(command, model, AddCommand.MESSAGE_DUPLICATE_PERSON);
        }
    }

    @Test
    public void execute_nameDuplicatesHiddenPerson_failure() throws Exception {
        showPersonAtIndex(model, INDEX_SECOND_PERSON);

        AddCommand command = new AddCommandParser().parse(" " + PREFIX_NAME + "  ALICE   PAULINE  "
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB);

        assertCommandFailure(command, model, AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model,
                AddCommand.MESSAGE_DUPLICATE_PERSON);
    }

}
