package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void execute_emptyModel_doesNotFail() {
        ModelManager model = new ModelManager(new seedu.address.model.AddressBook(), new UserPrefs());
        new GroupPostalCommand().execute(model);
        assertEquals(0, model.getFilteredPersonList().size());
    }
}
