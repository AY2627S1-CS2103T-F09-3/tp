package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORDER_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORDER_STATUS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
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
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds or edits the preorder details of the person identified"
            + "by the index number used in the last person listing."
            + "Existing preorder details will be overwritten by the input. \n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_ORDER_DETAILS + "DETAILS] "
            + "[" + PREFIX_ORDER_STATUS + "STATUS]\n"
            + "Example: " + COMMAND_WORD + " 1 " + PREFIX_ORDER_DETAILS + "1 Chocolate Cake "
            + PREFIX_ORDER_STATUS + "PENDING";
    public static final String MESSAGE_NO_ORDER_YET = "Identified person has no existing preorder. Please specify"
            + " preorder details and preorder status";
    public static final String MESSAGE_ADD_ORDER_SUCCESS = "Added preorder to Person: %1$s";
    public static final String MESSAGE_EDIT_ORDER_SUCCESS = "Edited preorder of Person: %1$s";

    public final Index index;
    public final String preorderDetailString;
    public final String predorderStatusString;

    /**
     * Creates an OrderCommand to add/edit {@code preorder} from the person at {@code index} in the filtered person list.
     */
    public OrderCommand(Index index, String preorderDetailString, String preorderStatusString) {
        requireNonNull(index);
        requireNonNull(preorderDetailString);
        requireNonNull(preorderStatusString);
        this.index = index;
        this.preorderDetailString = preorderDetailString;
        this.predorderStatusString = preorderStatusString;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();
        Person identifiedPerson = getIdentifiedPerson(lastShownList);
        Person personWithUpdatedOrder = createUpdatedOrderPerson(identifiedPerson, this.preorderDetailString,
                this.predorderStatusString);

        model.setPerson(identifiedPerson, personWithUpdatedOrder);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        return new CommandResult(generateSuccessMessage(personWithUpdatedOrder));
    }

    private Person getIdentifiedPerson(List<Person> lastShownList) throws CommandException {
        if (this.index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return lastShownList.get(this.index.getZeroBased());
    }

    private static Person createUpdatedOrderPerson(Person personToUpdateOrder, String preorderDetailString,
                                                  String preorderStatusString) throws CommandException {
        assert personToUpdateOrder != null;
        assert preorderDetailString != null;
        assert preorderStatusString != null;

        Optional<Preorder> existingPreorder = personToUpdateOrder.getPreorder();
        Preorder newPreorder;
        PreorderStatus newPreorderStatus;
        PreorderDetails newPreorderDetails;

        if (!existingPreorder.isEmpty()) {
            Preorder oldPreorder = existingPreorder.get();
            PreorderDetails oldPreorderDetails = oldPreorder.getDetails();
            PreorderStatus oldPreorderStatus = oldPreorder.getStatus();
            if (preorderDetailString.isEmpty() && !preorderStatusString.isEmpty()) {
                newPreorderStatus = PreorderStatus.valueOf(preorderStatusString);
                newPreorder = new Preorder(oldPreorderDetails, newPreorderStatus);
                return new Person(personToUpdateOrder.getName(), personToUpdateOrder.getPhone(),
                        personToUpdateOrder.getEmail(), personToUpdateOrder.getAddress(), personToUpdateOrder.getTags(),
                        Optional.of(newPreorder));
            } else if (!preorderDetailString.isEmpty() && preorderStatusString.isEmpty()) {
                newPreorderDetails = new PreorderDetails(preorderDetailString);
                newPreorder = new Preorder(newPreorderDetails, oldPreorderStatus);
                return new Person(personToUpdateOrder.getName(), personToUpdateOrder.getPhone(),
                        personToUpdateOrder.getEmail(), personToUpdateOrder.getAddress(), personToUpdateOrder.getTags(),
                        Optional.of(newPreorder));
            }
        }

        if (isArgumentsEmpty(preorderDetailString, preorderStatusString)) {
            throw new CommandException(MESSAGE_NO_ORDER_YET);
        }

        newPreorderDetails = new PreorderDetails(preorderDetailString);
        newPreorderStatus = PreorderStatus.valueOf(preorderStatusString);
        newPreorder = new Preorder(newPreorderDetails, newPreorderStatus);

        return new Person(personToUpdateOrder.getName(), personToUpdateOrder.getPhone(), personToUpdateOrder.getEmail(),
                personToUpdateOrder.getAddress(), personToUpdateOrder.getTags(), Optional.of(newPreorder));
    }

    private static boolean isArgumentsEmpty(String preorderDetailString, String predorderStatusString) {
        return preorderDetailString.isEmpty() || predorderStatusString.isEmpty();
    }

    private String generateSuccessMessage(Person personToUpdateOrder) {
        String messageFormat = (preorderDetailString.isEmpty() && predorderStatusString.isEmpty())
                ? MESSAGE_ADD_ORDER_SUCCESS : MESSAGE_EDIT_ORDER_SUCCESS;
        return String.format(messageFormat, Messages.format(personToUpdateOrder));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof OrderCommand)) {
            return false;
        }

        OrderCommand e = (OrderCommand) other;
        return this.index.equals(e.index)
                && this.preorderDetailString.equals(e.preorderDetailString)
                && this.predorderStatusString.equals(e.predorderStatusString);
    }

}
