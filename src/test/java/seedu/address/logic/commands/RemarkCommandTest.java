package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

public class RemarkCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndex_updatesRemark() {
        Person original = model.getFilteredPersonList()
                .get(INDEX_FIRST_PERSON.getZeroBased());
        Person expected = new Person(original.getName(), original.getPhone(), original.getEmail(),
                original.getAddress(),
                new Remark("Likes baseball"), original.getTags());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(original, expected);

        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes baseball")), model,
                String.format(RemarkCommand.MESSAGE_SUCCESS, Messages.format(expected)), expectedModel);
    }

    @Test
    public void execute_invalidIndex_throwsCommandException() {
        assertCommandFailure(new RemarkCommand(seedu.address.commons.core.index.Index
                .fromOneBased(model.getFilteredPersonList().size() + 1), new Remark("test")), model,
                Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_emptyRemark_clearsRemark() {
        Person original = model.getFilteredPersonList()
                .get(INDEX_FIRST_PERSON.getZeroBased());
        Person withRemark = new Person(original.getName(), original.getPhone(), original.getEmail(),
                original.getAddress(),
                new Remark("Existing remark"), original.getTags());
        model.setPerson(original, withRemark);

        Person cleared = new Person(withRemark.getName(), withRemark.getPhone(), withRemark.getEmail(),
                withRemark.getAddress(), new Remark(""), withRemark.getTags());
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.setPerson(withRemark, cleared);

        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("")), model,
                String.format(RemarkCommand.MESSAGE_SUCCESS, Messages.format(cleared)), expectedModel);
    }
}
