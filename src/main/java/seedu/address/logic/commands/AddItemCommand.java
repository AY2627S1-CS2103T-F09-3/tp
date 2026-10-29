package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_QUANTITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.Preorder;

/**
 * Appends one item to the preorder of a person identified by their displayed index.
 * Creates the preorder if absent and preserves all existing items and customer details.
 */
public class AddItemCommand extends Command {
    public static final String COMMAND_WORD = "additem";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds one item to the preorder of the customer in the displayed list. "
            + "A preorder is created if the customer does not have one.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_DETAILS + "NAME " + PREFIX_QUANTITY + "QUANTITY "
            + PREFIX_STATUS + "STATUS [" + PREFIX_REMARK + "REMARK]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_DETAILS + "Pineapple Tarts "
            + PREFIX_QUANTITY + "2 " + PREFIX_STATUS + "PENDING " + PREFIX_REMARK + "Less sugar";
    public static final String MESSAGE_ADD_ITEM_SUCCESS =
            "Added item %1$d for %2$s: %3$s (quantity: %4$s, %5$s)";
    public static final String MESSAGE_QUANTITY_REQUIRED = "New items must have a quantity.";

    private final Index index;
    private final Item item;

    /**
     * Creates a command to append an immutable item with a known quantity.
     *
     * @throws NullPointerException if either argument is null.
     * @throws IllegalArgumentException if the item has no quantity.
     */
    public AddItemCommand(Index index, Item item) {
        requireNonNull(index);
        requireNonNull(item);
        checkArgument(item.getQuantity().isPresent(), MESSAGE_QUANTITY_REQUIRED);
        this.index = index;
        this.item = item;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        Person identifiedPerson = getIdentifiedPerson(lastShownList);
        Person personWithAddedItem = createPersonWithAddedItem(identifiedPerson, item);
        model.setPerson(identifiedPerson, personWithAddedItem);

        return new CommandResult(String.format(MESSAGE_ADD_ITEM_SUCCESS,
                personWithAddedItem.getPreorder().orElseThrow().getItems().size(), identifiedPerson.getName(),
                item.getName(), item.getQuantity().orElseThrow(), item.getStatus()));
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
     * Appends the item to a copy of the existing list, preserving fulfilment details and all other person data.
     */
    private Person createPersonWithAddedItem(Person personToAddItem, Item newItem) {
        List<Item> items = new ArrayList<>(personToAddItem.getPreorder()
                .map(Preorder::getItems).orElseGet(List::of));
        items.add(newItem);
        Preorder updatedPreorder = personToAddItem.getPreorder()
                .map(preorder -> preorder.withItems(items)).orElseGet(() -> new Preorder(items));
        return new Person(personToAddItem.getName(), personToAddItem.getPhone(), personToAddItem.getEmail(),
                personToAddItem.getAddress(), personToAddItem.getPostalCode(), personToAddItem.getTags(),
                Optional.of(updatedPreorder));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof AddItemCommand otherAddItemCommand)) {
            return false;
        }
        return index.equals(otherAddItemCommand.index) && item.equals(otherAddItemCommand.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), item);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("item", item)
                .toString();
    }
}
