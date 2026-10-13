package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.PreorderStatusPredicate;
import seedu.address.model.preorder.PreorderStatus;

/**
 * Lists customers with active preorders, or with preorders matching a specified fulfillment status.
 */
public class TrackCommand extends Command {

    public static final String COMMAND_WORD = "track";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Displays customers with orders, optionally filtered by fulfilment status.\n"
            + "Parameters: [s/STATUS]\n"
            + "Example: " + COMMAND_WORD + " s/PENDING";

    public static final String MESSAGE_ORDERS_LISTED_OVERVIEW = "%1$d order(s) listed!";
    public static final String MESSAGE_ORDERS_BY_STATUS_LISTED_OVERVIEW = "%1$d order(s) for %2$s listed!";

    private final Optional<PreorderStatus> status;

    /**
     * Creates a command that lists preorders with the specified {@code status}.
     * An empty {@code status} lists active preorders: PENDING and PREPARED.
     */
    public TrackCommand(Optional<PreorderStatus> status) {
        this.status = requireNonNull(status);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        Set<PreorderStatus> statuses = status.map(Set::of)
                .orElse(Set.of(PreorderStatus.PENDING, PreorderStatus.PREPARED));
        model.updateFilteredPersonList(new PreorderStatusPredicate(statuses));

        int orderCount = model.getFilteredPersonList().size();
        String feedback = String.format(MESSAGE_ORDERS_LISTED_OVERVIEW, orderCount);
        if (orderCount > 0 && status.isPresent()) {
            feedback = String.format(MESSAGE_ORDERS_BY_STATUS_LISTED_OVERVIEW, orderCount, status.get());
        }
        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof TrackCommand otherTrackCommand)) {
            return false;
        }

        return status.equals(otherTrackCommand.status);
    }

    @Override
    public int hashCode() {
        return status.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("status", status).toString();
    }
}
