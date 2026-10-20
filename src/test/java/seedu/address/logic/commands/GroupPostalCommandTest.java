package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.testutil.PersonBuilder;

public class GroupPostalCommandTest {
    @Test
    public void execute_groupsByPostalCodeAndMissingLast() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Missing").withPostalCode(null).build());
        model.addPerson(new PersonBuilder().withName("Later").withPostalCode("654321").build());
        model.addPerson(new PersonBuilder().withName("Earlier").withPostalCode("012345").build());
        model.addPerson(new PersonBuilder().withName("Same").withPostalCode("012345").build());

        new GroupPostalCommand().execute(model);

        assertEquals("012345", model.getFilteredPersonList().get(0).getPostalCode().value);
        assertEquals("012345", model.getFilteredPersonList().get(1).getPostalCode().value);
        assertEquals("654321", model.getFilteredPersonList().get(2).getPostalCode().value);
        assertEquals("Missing", model.getFilteredPersonList().get(3).getName().fullName);
    }

    @Test
    public void execute_groupsByPostalPrefix() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Prefix54").withPostalCode("541111").build());
        model.addPerson(new PersonBuilder().withName("Prefix53Late").withPostalCode("539999").build());
        model.addPerson(new PersonBuilder().withName("Prefix53Early").withPostalCode("531234").build());

        new GroupPostalCommand().execute(model);

        assertEquals("531234", model.getFilteredPersonList().get(0).getPostalCode().value);
        assertEquals("539999", model.getFilteredPersonList().get(1).getPostalCode().value);
        assertEquals("541111", model.getFilteredPersonList().get(2).getPostalCode().value);
    }

    @Test
    public void execute_preservesActiveFilterAndDoesNotDuplicateCustomers() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Visible").withPostalCode("531234").build());
        model.addPerson(new PersonBuilder().withName("Hidden").withPostalCode("541234").build());
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Visible"));

        new GroupPostalCommand().execute(model);

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("Visible", model.getFilteredPersonList().get(0).getName().fullName);
        assertFalse(model.getFilteredPersonList().stream()
                .anyMatch(person -> person.getName().fullName.equals("Hidden")));
    }

    @Test
    public void execute_emptyModel_doesNotFail() {
        ModelManager model = new ModelManager(new seedu.address.model.AddressBook(), new UserPrefs());
        new GroupPostalCommand().execute(model);
        assertEquals(0, model.getFilteredPersonList().size());
    }
}
