package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ITEM_INDEX;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.Preorder;

/**
 * Deletes one numbered item of a customer identified using their displayed index.
 * Retains the preorder even when its final item is removed.
 */
public class DeleteItemCommand extends Command {

    public static final String COMMAND_WORD = "deleteitem";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes one numbered item of the customer in the displayed list. "
            + "The preorder is retained even when its final item is removed.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_ITEM_INDEX + "ITEM_INDEX (must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_ITEM_INDEX + "2";

    public static final String MESSAGE_DELETE_ITEM_SUCCESS =
            "Deleted item %1$d for %2$s: %3$s (quantity: %4$s, %5$s)";
    public static final String MESSAGE_NO_PREORDER = "This customer has no preorder.";
    public static final String MESSAGE_INVALID_ITEM_INDEX = "The item index provided is invalid.";

    private static final Logger logger = LogsCenter.getLogger(DeleteItemCommand.class);

    private final Index index;
    private final Index itemIndex;

    /**
     * Creates a command to delete the item at {@code itemIndex} from the displayed customer at {@code index}.
     *
     * @throws NullPointerException if either index is null.
     */
    public DeleteItemCommand(Index index, Index itemIndex) {
        requireNonNull(index);
        requireNonNull(itemIndex);
        this.index = index;
        this.itemIndex = itemIndex;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        Person identifiedPerson = getIdentifiedPerson(lastShownList);

        Item itemToDelete = getIdentifiedItem(identifiedPerson);
        Person personWithUpdatedPreorder = createPersonWithoutItem(identifiedPerson);
        model.setPerson(identifiedPerson, personWithUpdatedPreorder);

        logger.info("Deleted item " + itemIndex.getOneBased() + " for " + identifiedPerson.getName());
        return new CommandResult(String.format(MESSAGE_DELETE_ITEM_SUCCESS, itemIndex.getOneBased(),
                identifiedPerson.getName(), itemToDelete.getName(),
                itemToDelete.getQuantity(), itemToDelete.getStatus()));
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
     * Returns the selected item, rejecting absent preorders and out-of-range item indices.
     */
    private Item getIdentifiedItem(Person person) throws CommandException {
        Preorder preorder = person.getPreorder().orElseThrow(() -> new CommandException(MESSAGE_NO_PREORDER));
        if (itemIndex.getZeroBased() >= preorder.getItems().size()) {
            throw new CommandException(MESSAGE_INVALID_ITEM_INDEX);
        }
        return preorder.getItems().get(itemIndex.getZeroBased());
    }

    /**
     * Removes only the validated item index, preserving remaining item order and all other customer data.
     */
    private Person createPersonWithoutItem(Person personWithPreorder) {
        assert personWithPreorder.getPreorder().isPresent() : "Preorder was validated before deleting";
        Preorder existingPreorder = personWithPreorder.getPreorder().orElseThrow();
        List<Item> items = new ArrayList<>(existingPreorder.getItems());
        assert itemIndex.getZeroBased() < items.size() : "Item index was validated before deleting";
        items.remove(itemIndex.getZeroBased());
        Preorder updatedPreorder = existingPreorder.withItems(items);
        return new Person(personWithPreorder.getName(), personWithPreorder.getPhone(),
                personWithPreorder.getEmail(), personWithPreorder.getAddress(), personWithPreorder.getPostalCode(),
                personWithPreorder.getTags(), Optional.of(updatedPreorder));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteItemCommand otherDeleteItemCommand)) {
            return false;
        }

        return index.equals(otherDeleteItemCommand.index) && itemIndex.equals(otherDeleteItemCommand.itemIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), itemIndex.getZeroBased());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("itemIndex", itemIndex)
                .toString();
    }
}
