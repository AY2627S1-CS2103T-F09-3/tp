package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.search.SearchCriteria;

/**
 * Finds and lists customers who satisfy every supplied search criterion.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Finds customers matching all supplied criteria and displays them with index numbers.\n"
            + "Parameters: [" + PREFIX_NAME + "NAME] [" + PREFIX_PHONE + "PHONE] ["
            + PREFIX_EMAIL + "EMAIL] [" + PREFIX_ADDRESS + "ADDRESS] [" + PREFIX_TAG + "TAG] ["
            + PREFIX_DETAILS + "PRODUCT] [" + PREFIX_STATUS + "STATUS]\n"
            + "Supply at least one criterion; use each prefix at most once.\n"
            + "Products match complete names ignoring case. Product and status must match the same item.\n"
            + "Omit s/ to include all statuses; otherwise use PENDING, PREPARED or COMPLETED.\n"
            + "Example: " + COMMAND_WORD + " " + PREFIX_NAME + "Alice " + PREFIX_TAG + "regular";

    private final SearchCriteria criteria;

    /**
     * Creates a command that lists customers satisfying {@code criteria}.
     */
    public FindCommand(SearchCriteria criteria) {
        this.criteria = requireNonNull(criteria);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(criteria);
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return criteria.equals(otherFindCommand.criteria);
    }

    @Override
    public int hashCode() {
        return criteria.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("criteria", criteria)
                .toString();
    }
}
