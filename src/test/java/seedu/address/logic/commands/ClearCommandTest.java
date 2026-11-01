package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.testutil.PersonBuilder;

public class ClearCommandTest {

    @Test
    public void execute_emptyAddressBook_success() {
        Model model = new ModelManager();
        Model expectedModel = new ModelManager();

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_nonEmptyAddressBook_success() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel.setAddressBook(new AddressBook());

        assertCommandSuccess(new ClearCommand(), model, ClearCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_withActiveSearchAndOrdering_resetsViewState() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        SearchCriteria criteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Alex")));
        model.updateFilteredPersonList(criteria);
        model.updatePersonListComparator(Comparator.comparing(person -> person.getName().fullName));

        CommandResult result = new ClearCommand().execute(model);

        assertEquals(ClearCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(Model.SEARCH_CRITERIA_SHOW_ALL_PERSONS, model.getActiveSearchCriteria());

        Person zoe = new PersonBuilder().withName("Zoe").build();
        Person amy = new PersonBuilder().withName("Amy").build();
        model.addPerson(zoe);
        model.addPerson(amy);
        assertEquals(List.of(zoe, amy), model.getFilteredPersonList());
    }

}
