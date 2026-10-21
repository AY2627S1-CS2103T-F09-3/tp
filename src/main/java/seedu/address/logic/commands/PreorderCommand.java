package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULFILMENT_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULFILMENT_METHOD;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HANDOVER_NOTES;

import java.time.LocalDate;
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
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.Preorder;

/**
 * Sets the fulfilment details shared by all items in the preorder of a customer identified using their
 * displayed index. Items and the customer's other details are kept.
 */
public class PreorderCommand extends Command {

    public static final String COMMAND_WORD = "preorder";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Sets the delivery or collection details shared by all items in the preorder of the customer "
            + "identified by the index number used in the displayed list. "
            + "Only supplied fields change; an empty value clears that field.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_FULFILMENT_DATE + "DATE] "
            + "[" + PREFIX_FULFILMENT_METHOD + "METHOD] "
            + "[" + PREFIX_HANDOVER_NOTES + "HANDOVER_NOTES]\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_FULFILMENT_DATE + "2026-12-20 "
            + PREFIX_FULFILMENT_METHOD + "delivery "
            + PREFIX_HANDOVER_NOTES + "Call on arrival";

    public static final String MESSAGE_NOT_CHANGED = "At least one of " + PREFIX_FULFILMENT_DATE + "DATE, "
            + PREFIX_FULFILMENT_METHOD + "METHOD or " + PREFIX_HANDOVER_NOTES + "HANDOVER_NOTES must be provided.";
    public static final String MESSAGE_SUCCESS = "Updated preorder details for %1$s: %2$s";
    public static final String MESSAGE_PAST_DATE_NOTE = "\nNote: %1$s is in the past.";
    public static final String MESSAGE_NOTHING_TO_CLEAR =
            "%1$s has no preorder, so there is nothing to clear. No changes made.";

    private static final Logger logger = LogsCenter.getLogger(PreorderCommand.class);

    private final Index index;
    private final EditFulfilmentDescriptor editFulfilmentDescriptor;

    /**
     * Creates a PreorderCommand to update the fulfilment details of the customer at {@code index}
     * in the displayed list.
     *
     * @param index Index of the customer in the displayed list.
     * @param editFulfilmentDescriptor Fulfilment details to set or clear.
     */
    public PreorderCommand(Index index, EditFulfilmentDescriptor editFulfilmentDescriptor) {
        requireNonNull(index);
        requireNonNull(editFulfilmentDescriptor);

        this.index = index;
        this.editFulfilmentDescriptor = new EditFulfilmentDescriptor(editFulfilmentDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        assert editFulfilmentDescriptor.isAnyFieldChanged() : "Parser rejects commands without any field";

        List<Person> lastShownList = model.getFilteredPersonList();
        Person identifiedPerson = getIdentifiedPerson(lastShownList);

        if (identifiedPerson.getPreorder().isEmpty() && !editFulfilmentDescriptor.hasNewValue()) {
            return new CommandResult(String.format(MESSAGE_NOTHING_TO_CLEAR, identifiedPerson.getName()));
        }

        Preorder currentPreorder = identifiedPerson.getPreorder().orElse(new Preorder(List.of()));
        Preorder updatedPreorder = createUpdatedPreorder(currentPreorder, editFulfilmentDescriptor);
        Person updatedPerson = createPersonWithPreorder(identifiedPerson, updatedPreorder);
        model.setPerson(identifiedPerson, updatedPerson);

        logger.info("Updated preorder details for " + identifiedPerson.getName());
        return new CommandResult(createSuccessMessage(identifiedPerson, updatedPreorder));
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
     * Creates and returns a {@code Preorder} with the items of {@code currentPreorder} and its fulfilment
     * details updated with {@code descriptor}. Details that were not supplied are kept.
     */
    private static Preorder createUpdatedPreorder(Preorder currentPreorder, EditFulfilmentDescriptor descriptor) {
        Optional<FulfilmentDate> date = descriptor.isDateChanged()
                ? descriptor.getDate() : currentPreorder.getDate();
        Optional<FulfilmentMethod> method = descriptor.isMethodChanged()
                ? descriptor.getMethod() : currentPreorder.getMethod();
        Optional<HandoverNotes> handoverNotes = descriptor.isHandoverNotesChanged()
                ? descriptor.getHandoverNotes() : currentPreorder.getHandoverNotes();

        return new Preorder(currentPreorder.getItems(), date, method, handoverNotes);
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code person} and {@code preorder} as their preorder.
     */
    private static Person createPersonWithPreorder(Person person, Preorder preorder) {
        return new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getPostalCode(), person.getTags(), Optional.of(preorder));
    }

    /**
     * Returns the success message showing the updated details, with a note if a past date was supplied.
     */
    private String createSuccessMessage(Person person, Preorder updatedPreorder) {
        String message = String.format(MESSAGE_SUCCESS, person.getName(),
                Messages.formatFulfilmentDetails(updatedPreorder));
        Optional<FulfilmentDate> suppliedDate = editFulfilmentDescriptor.getDate();
        if (suppliedDate.isPresent() && isPastDate(suppliedDate.get())) {
            message += String.format(MESSAGE_PAST_DATE_NOTE, suppliedDate.get().toDisplayString());
        }
        return message;
    }

    /**
     * Returns true if {@code date} is before today.
     */
    private static boolean isPastDate(FulfilmentDate date) {
        return date.value.isBefore(LocalDate.now());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof PreorderCommand otherPreorderCommand)) {
            return false;
        }

        return index.equals(otherPreorderCommand.index)
                && editFulfilmentDescriptor.equals(otherPreorderCommand.editFulfilmentDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editFulfilmentDescriptor", editFulfilmentDescriptor)
                .toString();
    }

    /**
     * Stores the fulfilment details to set or clear. A field is changed only if its prefix was supplied;
     * a changed field with no value clears that detail.
     */
    public static class EditFulfilmentDescriptor {
        private FulfilmentDate date;
        private FulfilmentMethod method;
        private HandoverNotes handoverNotes;
        private boolean isDateChanged;
        private boolean isMethodChanged;
        private boolean isHandoverNotesChanged;

        public EditFulfilmentDescriptor() {}

        /**
         * Copy constructor.
         */
        public EditFulfilmentDescriptor(EditFulfilmentDescriptor toCopy) {
            requireNonNull(toCopy);
            date = toCopy.date;
            method = toCopy.method;
            handoverNotes = toCopy.handoverNotes;
            isDateChanged = toCopy.isDateChanged;
            isMethodChanged = toCopy.isMethodChanged;
            isHandoverNotesChanged = toCopy.isHandoverNotesChanged;
        }

        /**
         * Returns true if at least one detail is set or cleared.
         */
        public boolean isAnyFieldChanged() {
            return isDateChanged || isMethodChanged || isHandoverNotesChanged;
        }

        /**
         * Returns true if at least one detail is given a new value, rather than only cleared.
         */
        public boolean hasNewValue() {
            return date != null || method != null || handoverNotes != null;
        }

        /**
         * Sets the date to apply, or null to clear the date.
         */
        public void setDate(FulfilmentDate date) {
            this.date = date;
            isDateChanged = true;
        }

        public Optional<FulfilmentDate> getDate() {
            return Optional.ofNullable(date);
        }

        public boolean isDateChanged() {
            return isDateChanged;
        }

        /**
         * Sets the method to apply, or null to clear the method.
         */
        public void setMethod(FulfilmentMethod method) {
            this.method = method;
            isMethodChanged = true;
        }

        public Optional<FulfilmentMethod> getMethod() {
            return Optional.ofNullable(method);
        }

        public boolean isMethodChanged() {
            return isMethodChanged;
        }

        /**
         * Sets the handover notes to apply, or null to clear the handover notes.
         */
        public void setHandoverNotes(HandoverNotes handoverNotes) {
            this.handoverNotes = handoverNotes;
            isHandoverNotesChanged = true;
        }

        public Optional<HandoverNotes> getHandoverNotes() {
            return Optional.ofNullable(handoverNotes);
        }

        public boolean isHandoverNotesChanged() {
            return isHandoverNotesChanged;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditFulfilmentDescriptor otherDescriptor)) {
                return false;
            }

            return Objects.equals(date, otherDescriptor.date)
                    && method == otherDescriptor.method
                    && Objects.equals(handoverNotes, otherDescriptor.handoverNotes)
                    && isDateChanged == otherDescriptor.isDateChanged
                    && isMethodChanged == otherDescriptor.isMethodChanged
                    && isHandoverNotesChanged == otherDescriptor.isHandoverNotesChanged;
        }

        @Override
        public int hashCode() {
            return Objects.hash(date, method, handoverNotes, isDateChanged, isMethodChanged, isHandoverNotesChanged);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("date", date)
                    .add("isDateChanged", isDateChanged)
                    .add("method", method)
                    .add("isMethodChanged", isMethodChanged)
                    .add("handoverNotes", handoverNotes)
                    .add("isHandoverNotesChanged", isHandoverNotesChanged)
                    .toString();
        }
    }
}
