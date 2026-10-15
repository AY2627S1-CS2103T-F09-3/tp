package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORDER_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORDER_STATUS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.PreorderDetails;
import seedu.address.model.preorder.PreorderStatus;

/**
 * Adds or updates the preorder of a person identified by their index in the displayed list.
 * Creating a preorder requires both fields; updating one preserves omitted fields.
 */
public class OrderCommand extends Command {
    public static final String COMMAND_WORD = "order";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds or edits the preorder details of the person identified "
            + "by the index number used in the last person listing. "
            + "Only supplied preorder fields will be updated. Both fields are required for a new preorder.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_ORDER_DETAILS + "DETAILS] "
            + "[" + PREFIX_ORDER_STATUS + "STATUS]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_ORDER_DETAILS + "1 Chocolate Cake "
            + PREFIX_ORDER_STATUS + "PENDING";
    public static final String MESSAGE_NO_ORDER_YET = "Identified person has no existing preorder. Please specify"
            + " preorder details and preorder status";
    public static final String MESSAGE_ADD_ORDER_SUCCESS = "Added preorder to %1$s: %2$s (%3$s)";
    public static final String MESSAGE_EDIT_ORDER_SUCCESS = "Edited preorder of %1$s: %2$s (%3$s)";
    public static final String MESSAGE_NOT_EDITED = "No preorder changes specified.";

    private final Index index;
    private final EditPreorderDescriptor editPreorderDescriptor;

    /**
     * Creates a command with a defensive copy of the supplied preorder changes.
     *
     * @param index Index of the person in the currently displayed list.
     * @param editPreorderDescriptor Validated fields to apply; omitted fields are left unchanged.
     */
    public OrderCommand(Index index, EditPreorderDescriptor editPreorderDescriptor) {
        requireNonNull(index);
        requireNonNull(editPreorderDescriptor);
        this.index = index;
        this.editPreorderDescriptor = new EditPreorderDescriptor(editPreorderDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        Person identifiedPerson = getIdentifiedPerson(lastShownList);
        if (identifiedPerson.getPreorder().isPresent()
                && !editPreorderDescriptor.isAnyFieldEdited()) {
            return new CommandResult(MESSAGE_NOT_EDITED);
        }

        Person personWithUpdatedOrder = createUpdatedOrderPerson(identifiedPerson);
        model.setPerson(identifiedPerson, personWithUpdatedOrder);

        return new CommandResult(generateSuccessMessage(identifiedPerson, personWithUpdatedOrder));
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
     * Creates a person with the supplied preorder fields, preserving all other person data.
     *
     * @throws CommandException if a new preorder is missing details or status.
     */
    private Person createUpdatedOrderPerson(Person personToUpdate) throws CommandException {
        Optional<Preorder> existingPreorder = personToUpdate.getPreorder();
        Optional<PreorderDetails> suppliedDetails = editPreorderDescriptor.getDetails();
        Optional<PreorderStatus> suppliedStatus = editPreorderDescriptor.getStatus();

        if (existingPreorder.isEmpty() && (suppliedDetails.isEmpty() || suppliedStatus.isEmpty())) {
            throw new CommandException(MESSAGE_NO_ORDER_YET);
        }

        PreorderDetails details = suppliedDetails
                .orElseGet(() -> existingPreorder.orElseThrow().getDetails());
        PreorderStatus status = suppliedStatus
                .orElseGet(() -> existingPreorder.orElseThrow().getStatus());
        Preorder updatedPreorder = new Preorder(details, status);
        return new Person(personToUpdate.getName(), personToUpdate.getPhone(), personToUpdate.getEmail(),
                personToUpdate.getAddress(), personToUpdate.getTags(), Optional.of(updatedPreorder));
    }

    /**
     * Formats feedback according to whether the original person already had a preorder.
     */
    private String generateSuccessMessage(Person originalPerson, Person updatedPerson) {
        assert updatedPerson.getPreorder().isPresent();

        String messageFormat = originalPerson.getPreorder().isEmpty() ? MESSAGE_ADD_ORDER_SUCCESS
                : MESSAGE_EDIT_ORDER_SUCCESS;
        Preorder currPreorder = updatedPerson.getPreorder().get();
        return String.format(messageFormat, updatedPerson.getName(),
                currPreorder.getDetails(), currPreorder.getStatus());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof OrderCommand otherOrderCommand)) {
            return false;
        }

        return index.equals(otherOrderCommand.index)
                && editPreorderDescriptor.equals(otherOrderCommand.editPreorderDescriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), editPreorderDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPreorderDescriptor", editPreorderDescriptor)
                .toString();
    }

    /**
     * Stores validated preorder changes. A null field means that field was not supplied.
     */
    public static class EditPreorderDescriptor {
        private PreorderDetails details;
        private PreorderStatus status;

        /**
         * Creates a descriptor with no supplied fields.
         */
        public EditPreorderDescriptor() {}

        /**
         * Copies the supplied descriptor. Its field values are immutable and can be shared.
         */
        public EditPreorderDescriptor(EditPreorderDescriptor toCopy) {
            requireNonNull(toCopy);
            details = toCopy.details;
            status = toCopy.status;
        }

        /**
         * Returns whether at least one preorder field was supplied.
         */
        public boolean isAnyFieldEdited() {
            return details != null || status != null;
        }

        /**
         * Sets the details to apply, or null to leave existing details unchanged.
         */
        public void setDetails(PreorderDetails details) {
            this.details = details;
        }

        /**
         * Returns the supplied details, if any.
         */
        public Optional<PreorderDetails> getDetails() {
            return Optional.ofNullable(details);
        }

        /**
         * Sets the status to apply, or null to leave the existing status unchanged.
         */
        public void setStatus(PreorderStatus status) {
            this.status = status;
        }

        /**
         * Returns the supplied status, if any.
         */
        public Optional<PreorderStatus> getStatus() {
            return Optional.ofNullable(status);
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            if (!(other instanceof EditPreorderDescriptor otherDescriptor)) {
                return false;
            }

            return Objects.equals(details, otherDescriptor.details) && status == otherDescriptor.status;
        }

        @Override
        public int hashCode() {
            return Objects.hash(details, status);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("details", details)
                    .add("status", status)
                    .toString();
        }
    }
}
