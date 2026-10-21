package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Deletes the preorder of a customer identified using their displayed index, keeping the customer's other details.
 */
public class DeleteOrderCommand extends Command {

    public static final String COMMAND_WORD = "deleteorder";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the preorder of the customer identified by the index number used in the displayed "
            + "customer list.\n"
            + "Parameters: INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_DELETE_ORDER_SUCCESS = "Deleted preorder for %1$s";
    public static final String MESSAGE_NO_PREORDER = "%1$s has no preorder to delete.";

    private static final Logger logger = LogsCenter.getLogger(DeleteOrderCommand.class);

    private final Index index;

    /**
     * Creates a DeleteOrderCommand to delete the preorder of the customer at {@code index} in the displayed list.
     */
    public DeleteOrderCommand(Index index) {
        requireNonNull(index);
        this.index = index;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        Person identifiedPerson = getIdentifiedPerson(lastShownList);

        if (identifiedPerson.getPreorder().isEmpty()) {
            throw new CommandException(String.format(MESSAGE_NO_PREORDER, identifiedPerson.getName()));
        }

        Person personWithoutPreorder = createPersonWithoutPreorder(identifiedPerson);
        model.setPerson(identifiedPerson, personWithoutPreorder);

        logger.info("Deleted preorder for " + identifiedPerson.getName());
        return new CommandResult(String.format(MESSAGE_DELETE_ORDER_SUCCESS, identifiedPerson.getName()));
    }

    /**
     * Returns the person at the command's index in the displayed list.
     *
     * @throws CommandException if the index is outside the displayed list.
     */
    private Person getIdentifiedPerson(List<Person> lastShownList) throws CommandException {
        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return lastShownList.get(index.getZeroBased());
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personWithPreorder}
     * but without a preorder.
     */
    private static Person createPersonWithoutPreorder(Person personWithPreorder) {
        assert personWithPreorder.getPreorder().isPresent();

        return new Person(personWithPreorder.getName(), personWithPreorder.getPhone(),
                personWithPreorder.getEmail(), personWithPreorder.getAddress(), personWithPreorder.getTags(),
                Optional.empty());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteOrderCommand otherDeleteOrderCommand)) {
            return false;
        }

        return index.equals(otherDeleteOrderCommand.index);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .toString();
    }
}
