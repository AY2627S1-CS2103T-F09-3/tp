package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ITEM_INDEX;
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
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.preorder.Remark;

/**
 * Edits one item of a person identified by their index in the displayed list.
 * Omitted fields, other items and the customer's contact details are preserved.
 */
public class EditItemCommand extends Command {
    public static final String COMMAND_WORD = "edititem";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Edits one numbered item of the person in the displayed list. "
            + "Only supplied fields are updated. Empty r/ clears the remark.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + PREFIX_ITEM_INDEX + "ITEM_INDEX (must be a positive integer) "
            + "[" + PREFIX_DETAILS + "NAME] [" + PREFIX_QUANTITY + "QUANTITY] "
            + "[" + PREFIX_STATUS + "STATUS] [" + PREFIX_REMARK + "REMARK]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_ITEM_INDEX + "2 " + PREFIX_QUANTITY + "3";
    public static final String MESSAGE_NO_PREORDER = "Identified person has no preorder.";
    public static final String MESSAGE_INVALID_ITEM_INDEX = "The item index provided is invalid.";
    public static final String MESSAGE_EDIT_ITEM_SUCCESS =
            "Edited item %1$d for %2$s: %3$s (quantity: %4$s, %5$s)";
    public static final String MESSAGE_NOT_EDITED = "No item changes specified.";

    private final Index index;
    private final Index itemIndex;
    private final EditItemDescriptor editItemDescriptor;

    /**
     * Creates a command with a defensive copy of the supplied item changes.
     *
     * @param index Index of the person in the currently displayed list.
     * @param itemIndex Index of the item in the customer's full item list.
     * @param editItemDescriptor Validated fields to apply; omitted fields are left unchanged.
     */
    public EditItemCommand(Index index, Index itemIndex, EditItemDescriptor editItemDescriptor) {
        requireNonNull(index);
        requireNonNull(itemIndex);
        requireNonNull(editItemDescriptor);
        this.index = index;
        this.itemIndex = itemIndex;
        this.editItemDescriptor = new EditItemDescriptor(editItemDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        Person identifiedPerson = getIdentifiedPerson(lastShownList);
        Item originalItem = getIdentifiedItem(identifiedPerson);
        if (!editItemDescriptor.isAnyFieldEdited()) {
            throw new CommandException(MESSAGE_NOT_EDITED);
        }

        Item updatedItem = createUpdatedItem(originalItem);
        Person personWithUpdatedOrder = createUpdatedOrderPerson(identifiedPerson, updatedItem);
        model.setPerson(identifiedPerson, personWithUpdatedOrder);

        return new CommandResult(String.format(MESSAGE_EDIT_ITEM_SUCCESS, itemIndex.getOneBased(),
                identifiedPerson.getName(), updatedItem.getName(),
                updatedItem.getQuantity().map(Quantity::toString).orElse(""), updatedItem.getStatus()));
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
     * Applies supplied fields, preserving missing quantities on migrated items unless explicitly edited.
     */
    private Item createUpdatedItem(Item original) {
        ItemName name = editItemDescriptor.getName().orElse(original.getName());
        Optional<Quantity> quantity = editItemDescriptor.getQuantity().or(original::getQuantity);
        ItemStatus status = editItemDescriptor.getStatus().orElse(original.getStatus());
        Optional<Remark> remark = editItemDescriptor.getRemark().orElse(original.getRemark());
        return new Item(name, quantity, status, remark);
    }

    /**
     * Replaces only the validated item index, preserving fulfilment details and the customer's other data.
     */
    private Person createUpdatedOrderPerson(Person personToUpdate, Item updatedItem) {
        assert personToUpdate.getPreorder().isPresent() : "Preorder was validated before updating";
        Preorder existingPreorder = personToUpdate.getPreorder().orElseThrow();
        List<Item> items = new ArrayList<>(existingPreorder.getItems());
        assert itemIndex.getZeroBased() < items.size() : "Item index was validated before updating";
        items.set(itemIndex.getZeroBased(), updatedItem);
        Preorder updatedPreorder = existingPreorder.withItems(items);
        return new Person(personToUpdate.getName(), personToUpdate.getPhone(), personToUpdate.getEmail(),
                personToUpdate.getAddress(), personToUpdate.getPostalCode(), personToUpdate.getTags(),
                Optional.of(updatedPreorder));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof EditItemCommand otherEditItemCommand)) {
            return false;
        }

        return index.equals(otherEditItemCommand.index)
                && itemIndex.equals(otherEditItemCommand.itemIndex)
                && editItemDescriptor.equals(otherEditItemCommand.editItemDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), itemIndex.getZeroBased(), editItemDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("itemIndex", itemIndex)
                .add("editItemDescriptor", editItemDescriptor)
                .toString();
    }

    /**
     * Stores validated item changes. A null field means that field was not supplied.
     */
    public static class EditItemDescriptor {
        private ItemName name;
        private ItemStatus status;
        private Quantity quantity;
        // null means unchanged; Optional.empty() means explicitly remove the remark.
        private Optional<Remark> remark;

        /**
         * Creates a descriptor with no supplied fields.
         */
        public EditItemDescriptor() {}

        /**
         * Copies the supplied descriptor. Its field values are immutable and can be shared.
         */
        public EditItemDescriptor(EditItemDescriptor toCopy) {
            requireNonNull(toCopy);
            name = toCopy.name;
            status = toCopy.status;
            quantity = toCopy.quantity;
            remark = toCopy.remark;
        }

        /**
         * Returns whether at least one item field was supplied.
         */
        public boolean isAnyFieldEdited() {
            return name != null || status != null || quantity != null || remark != null;
        }

        /**
         * Sets the name to apply, or null to leave existing name unchanged.
         */
        public void setName(ItemName name) {
            this.name = name;
        }

        /**
         * Returns the supplied name, if any.
         */
        public Optional<ItemName> getName() {
            return Optional.ofNullable(name);
        }

        /**
         * Sets the status to apply, or null to leave the existing status unchanged.
         */
        public void setStatus(ItemStatus status) {
            this.status = status;
        }

        /**
         * Returns the supplied status, if any.
         */
        public Optional<ItemStatus> getStatus() {
            return Optional.ofNullable(status);
        }

        /**
         * Sets a supplied quantity, or null to preserve the original quantity.
         */
        public void setQuantity(Quantity quantity) {
            this.quantity = quantity;
        }

        /**
         * Returns the supplied quantity, if any.
         */
        public Optional<Quantity> getQuantity() {
            return Optional.ofNullable(quantity);
        }

        /**
         * Sets a remark replacement; an empty Optional clears it and null leaves it unchanged.
         */
        public void setRemark(Optional<Remark> remark) {
            this.remark = remark;
        }

        /**
         * Returns the supplied remark change; an empty inner Optional requests removal.
         */
        public Optional<Optional<Remark>> getRemark() {
            return Optional.ofNullable(remark);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            if (!(other instanceof EditItemDescriptor otherDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherDescriptor.name) && status == otherDescriptor.status
                    && Objects.equals(quantity, otherDescriptor.quantity)
                    && Objects.equals(remark, otherDescriptor.remark);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, status, quantity, remark);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("status", status)
                    .add("quantity", quantity)
                    .add("remark", remark)
                    .toString();
        }
    }
}
