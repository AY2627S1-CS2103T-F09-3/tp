package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
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

    public static final String MESSAGE_INVALID_FORMAT =
            "Format error. Input format: untag INDEX t/TAG. Example: untag 1 t/friends";
    public static final String MESSAGE_INVALID_INDEX_FORMAT =
            "Invalid index! Please enter a positive integer index between 1 and the size of the current list.";

    public static final String MESSAGE_UNTAG_PERSON_SUCCESS = "Removed tag(s) %1$s from %2$s";
    public static final String MESSAGE_TAG_NOT_FOUND = "%1$s does not have tag %2$s";

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
        requireNonNull(model);
        Person personToUntag = getPersonToUntag(model.getFilteredPersonList());
        Person untaggedPerson = createUntaggedPerson(personToUntag);

        model.setPerson(personToUntag, untaggedPerson);
        return new CommandResult(String.format(MESSAGE_UNTAG_PERSON_SUCCESS, tag, personToUntag.getName()));
    }

    /**
     * Returns the person at the requested index in the currently displayed list.
     *
     * @throws CommandException If the index is outside the displayed list.
     */
    private Person getPersonToUntag(List<Person> lastShownList) throws CommandException {
        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return lastShownList.get(index.getZeroBased());
    }

    /**
     * Copies the person with all case-insensitive matches of the requested tag removed.
     *
     * @throws CommandException If the person does not have the requested tag.
     */
    private Person createUntaggedPerson(Person personToUntag) throws CommandException {
        Set<Tag> updatedTags = new HashSet<>(personToUntag.getTags());
        boolean isTagRemoved = updatedTags.removeIf(existingTag -> existingTag.tagName.equalsIgnoreCase(tag.tagName));
        if (!isTagRemoved) {
            throw new CommandException(String.format(MESSAGE_TAG_NOT_FOUND, personToUntag.getName(), tag));
        }

        return new Person(personToUntag.getName(), personToUntag.getPhone(), personToUntag.getEmail(),
                personToUntag.getAddress(), updatedTags, personToUntag.getPreorder());
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
