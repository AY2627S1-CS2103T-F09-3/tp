package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.SearchCriteria;

/**
 * Lists customers with active items, or items matching a specified status.
 */
public class TrackCommand extends Command {

    public static final String COMMAND_WORD = "track";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Displays customers with matching preorder items.\n"
            + "Parameters: [" + PREFIX_STATUS + "STATUS]\n"
            + "Omit status for PENDING and PREPARED items; otherwise use PENDING, PREPARED or COMPLETED.\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_STATUS + "PENDING";

    public static final String MESSAGE_ITEMS_LISTED_OVERVIEW =
            "%1$d customer(s) with %2$d matching item(s) listed!";
    public static final String MESSAGE_ITEMS_BY_STATUS_LISTED_OVERVIEW =
            "%1$d customer(s) with %2$d matching item(s) for %3$s listed!";

    private final Optional<ItemStatus> status;

    /**
     * Creates a command that lists customers with items of the specified {@code status}.
     * An empty {@code status} lists active items: PENDING and PREPARED.
     */
    public TrackCommand(Optional<ItemStatus> status) {
        this.status = requireNonNull(status);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        ItemCriterion criterion = createItemCriterion();
        model.updateFilteredPersonList(new SearchCriteria(List.of(criterion)));
        List<Person> customers = model.getFilteredPersonList();
        long itemCount = customers.stream().mapToLong(person -> criterion.getMatchingItems(person).size()).sum();
        return new CommandResult(formatFeedback(customers.size(), itemCount));
    }

    private ItemCriterion createItemCriterion() {
        Set<ItemStatus> statuses = status.map(Set::of)
                .orElse(Set.of(ItemStatus.PENDING, ItemStatus.PREPARED));
        return new ItemCriterion(Optional.empty(), statuses);
    }

    private String formatFeedback(int customerCount, long itemCount) {
        if (status.isPresent()) {
            return String.format(MESSAGE_ITEMS_BY_STATUS_LISTED_OVERVIEW, customerCount, itemCount, status.get());
        }
        return String.format(MESSAGE_ITEMS_LISTED_OVERVIEW, customerCount, itemCount);
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
