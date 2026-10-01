package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

/** Changes the remark of an existing person. */
public class RemarkCommand extends Command {
    public static final String COMMAND_WORD = "remark";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a remark to the person identified by the index.\n"
            + "Parameters: INDEX (must be a positive integer) r/REMARK\n"
            + "Example: " + COMMAND_WORD + " 2 r/Likes baseball";
    public static final String MESSAGE_SUCCESS = "Added remark to person: %1$s";

    private final Index index;
    private final Remark remark;

    /**
     * Creates a command for replacing the remark at the given displayed index.
     *
     * @param index displayed person index
     * @param remark replacement remark
     */
    public RemarkCommand(Index index, Remark remark) {
        this.index = requireNonNull(index);
        this.remark = requireNonNull(remark);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person remarkedPerson = new Person(personToEdit.getName(), personToEdit.getPhone(), personToEdit.getEmail(),
                personToEdit.getAddress(), remark, personToEdit.getTags());
        model.setPerson(personToEdit, remarkedPerson);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(remarkedPerson)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof RemarkCommand otherRemarkCommand)) {
            return false;
        }
        return index.equals(otherRemarkCommand.index) && remark.equals(otherRemarkCommand.remark);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("index", index).add("remark", remark).toString();
    }
}
