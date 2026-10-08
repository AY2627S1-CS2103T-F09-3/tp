package seedu.address.logic.commands;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.tag.Tag;

/**
 * Removes a tag from a person identified using its displayed index from the address book.
 */
public class UntagCommand extends Command {

    public static final String COMMAND_WORD = "untag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes a tag from the person identified by the index number used in the displayed person list.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_TAG + "TAG\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_TAG + "friends";

    public static final String MESSAGE_NOT_IMPLEMENTED_YET = "Untag command not implemented yet";

    private final Index index;
    private final Tag tag;

    /**
     * Creates an UntagCommand to remove {@code tag} from the person at {@code index} in the filtered person list.
     */
    public UntagCommand(Index index, Tag tag) {
        requireAllNonNull(index, tag);
        this.index = index;
        this.tag = tag;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        throw new CommandException(MESSAGE_NOT_IMPLEMENTED_YET);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UntagCommand otherUntagCommand)) {
            return false;
        }

        return index.equals(otherUntagCommand.index)
                && tag.equals(otherUntagCommand.tag);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("tag", tag)
                .toString();
    }
}
