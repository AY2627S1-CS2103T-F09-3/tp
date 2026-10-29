package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
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
 * Adds a tag to a person identified using its displayed index from the address book.
 */
public class TagCommand extends Command {

    public static final String COMMAND_WORD = "tag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds a tag to the customer identified by the index number used in the displayed customer list.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_TAG + "TAG\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_TAG + "regular";

    public static final String MESSAGE_TAG_PERSON_SUCCESS = "Added tag(s) %1$s to %2$s";
    public static final String MESSAGE_DUPLICATE_TAG = "%1$s already has tag %2$s. No changes made.";

    private final Index index;
    private final Tag tag;

    /**
     * Creates a TagCommand to add {@code tag} to the person at {@code index} in the filtered person list.
     */
    public TagCommand(Index index, Tag tag) {
        requireNonNull(index);
        requireNonNull(tag);

        this.index = index;
        this.tag = tag;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToTag = lastShownList.get(index.getZeroBased());
        if (personToTag.getTags().contains(tag)) {
            return new CommandResult(String.format(MESSAGE_DUPLICATE_TAG, personToTag.getName(), tag));
        }

        Person taggedPerson = createTaggedPerson(personToTag, tag);

        model.setPerson(personToTag, taggedPerson);
        return new CommandResult(String.format(MESSAGE_TAG_PERSON_SUCCESS, tag, personToTag.getName()));
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToTag}
     * and {@code tag} added to its tags.
     */
    private static Person createTaggedPerson(Person personToTag, Tag tag) {
        assert personToTag != null;

        Set<Tag> updatedTags = new HashSet<>(personToTag.getTags());
        updatedTags.add(tag);

        return new Person(personToTag.getName(), personToTag.getPhone(), personToTag.getEmail(),
                personToTag.getAddress(), personToTag.getPostalCode(), updatedTags, personToTag.getPreorder());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TagCommand otherTagCommand)) {
            return false;
        }

        return index.equals(otherTagCommand.index)
                && tag.equals(otherTagCommand.tag);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("tag", tag)
                .toString();
    }
}
