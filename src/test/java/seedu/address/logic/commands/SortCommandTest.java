package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PreorderBuilder;

public class SortCommandTest {

    @Test
    public void execute_datesInRandomOrder_sortsEarliestFirst() {
        Person late = personWithDate("Late", "2026-12-24");
        Person early = personWithDate("Early", "2026-01-01");
        Person middle = personWithDate("Middle", "2026-06-15");
        ModelManager model = modelWith(late, early, middle);

        CommandResult result = new SortCommand().execute(model);

        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, 3), result.getFeedbackToUser());
        assertEquals(List.of(early, middle, late), model.getFilteredPersonList());
    }

    @Test
    public void execute_missingDateAndNoPreorder_listedLastByName() {
        Person noPreorder = new PersonBuilder().withName("Amy").build();
        Person noDate = new PersonBuilder().withName("Bob").withPreorder(new PreorderBuilder().build()).build();
        Person dated = personWithDate("Zoe", "2026-12-20");
        ModelManager model = modelWith(noDate, dated, noPreorder);

        new SortCommand().execute(model);

        assertEquals(List.of(dated, noPreorder, noDate), model.getFilteredPersonList());
    }

    @Test
    public void execute_equalDates_orderedByNameIgnoringCase() {
        Person bob = personWithDate("Bob", "2026-12-20");
        Person amy = personWithDate("amy", "2026-12-20");
        ModelManager model = modelWith(bob, amy);

        new SortCommand().execute(model);

        assertEquals(List.of(amy, bob), model.getFilteredPersonList());
    }

    @Test
    public void execute_activeFilter_keepsFilter() {
        Person visible = personWithDate("Visible", "2026-12-20");
        Person hidden = personWithDate("Hidden", "2026-01-01");
        ModelManager model = modelWith(visible, hidden);
        model.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Visible"))));

        CommandResult result = new SortCommand().execute(model);

        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, 1), result.getFeedbackToUser());
        assertEquals(List.of(visible), model.getFilteredPersonList());
    }

    @Test
    public void execute_emptyList_success() {
        ModelManager model = new ModelManager();

        CommandResult result = new SortCommand().execute(model);

        assertEquals(String.format(SortCommand.MESSAGE_SUCCESS, 0), result.getFeedbackToUser());
        assertTrue(model.getFilteredPersonList().isEmpty());
    }

    @Test
    public void equals() {
        SortCommand sortCommand = new SortCommand();

        assertTrue(sortCommand.equals(sortCommand));
        assertTrue(sortCommand.equals(new SortCommand()));
        assertFalse(sortCommand.equals(null));
        assertFalse(sortCommand.equals(new ListCommand()));
    }

    private static Person personWithDate(String name, String date) {
        Preorder preorder = new PreorderBuilder().withDate(date).build();
        return new PersonBuilder().withName(name).withPreorder(preorder).build();
    }

    private static ModelManager modelWith(Person... persons) {
        ModelManager model = new ModelManager();
        for (Person person : persons) {
            model.addPerson(person);
        }
        return model;
    }
}
