package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

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
            + ": Adds a tag to the person identified by the index number used in the displayed person list.\n"
            + "Parameters: INDEX (must be a positive integer) " + PREFIX_TAG + "TAG\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_TAG + "regular";

    public static final String MESSAGE_TAG_PERSON_SUCCESS = "Tagged person: %1$s";

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
        Person taggedPerson = createTaggedPerson(personToTag, tag);

        model.setPerson(personToTag, taggedPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_TAG_PERSON_SUCCESS, Messages.format(taggedPerson)));
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
                personToTag.getAddress(), updatedTags, personToTag.getPreorder());
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
