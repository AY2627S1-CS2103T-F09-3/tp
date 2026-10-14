package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORDER_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORDER_STATUS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.PreorderDetails;
import seedu.address.model.preorder.PreorderStatus;

public class OrderCommand extends Command {
    public static final String COMMAND_WORD = "order";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds or edits the preorder details of the person identified "
            + "by the index number used in the last person listing. "
            + "Only supplied preorder fields will be updated. Both fields are required for a new preorder.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_ORDER_DETAILS + "DETAILS] "
            + "[" + PREFIX_ORDER_STATUS + "STATUS]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_ORDER_DETAILS + "1 Chocolate Cake "
            + PREFIX_ORDER_STATUS + "PENDING";
    public static final String MESSAGE_NO_ORDER_YET = "Identified person has no existing preorder. Please specify"
            + " preorder details and preorder status";
    public static final String MESSAGE_ADD_ORDER_SUCCESS = "Added preorder to Person: %1$s";
    public static final String MESSAGE_EDIT_ORDER_SUCCESS = "Edited preorder of Person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "No preorder changes specified.";

    private final Index index;
    private final EditPreorderDescriptor editPreorderDescriptor;

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
                && editPreorderDescriptor.getDetails().isEmpty() && editPreorderDescriptor.getStatus().isEmpty()) {
            return new CommandResult(MESSAGE_NOT_EDITED);
        }
        Person personWithUpdatedOrder = createUpdatedOrderPerson(identifiedPerson);

        model.setPerson(identifiedPerson, personWithUpdatedOrder);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(generateSuccessMessage(identifiedPerson, personWithUpdatedOrder));
    }

    private Person getIdentifiedPerson(List<Person> lastShownList) throws CommandException {
        if (this.index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return lastShownList.get(this.index.getZeroBased());
    }

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

    private String generateSuccessMessage(Person originalPerson, Person updatedPerson) {
        String messageFormat = originalPerson.getPreorder().isEmpty() ? MESSAGE_ADD_ORDER_SUCCESS
                : MESSAGE_EDIT_ORDER_SUCCESS;
        return String.format(messageFormat, Messages.format(updatedPerson));
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

    public static class EditPreorderDescriptor {
        private PreorderDetails details;
        private PreorderStatus status;

        public EditPreorderDescriptor() {}

        public EditPreorderDescriptor(EditPreorderDescriptor toCopy) {
            details = toCopy.details;
            status = toCopy.status;
        }

        public void setDetails(PreorderDetails details) {
            this.details = details;
        }

        public Optional<PreorderDetails> getDetails() {
            return Optional.ofNullable(details);
        }

        public void setStatus(PreorderStatus status) {
            this.status = status;
        }

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
    }
}
