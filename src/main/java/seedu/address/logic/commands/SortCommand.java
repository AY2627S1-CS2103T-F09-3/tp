package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.Preorder;

/**
 * Sorts the currently displayed customers by the fulfilment date of their preorder.
 */
public class SortCommand extends Command {

    public static final String COMMAND_WORD = "sort";
    public static final String DATE_ARGUMENT = "date";

    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + DATE_ARGUMENT
            + ": Sorts the displayed customers by fulfilment date, earliest first. "
            + "Customers without a date are listed last.\n"
            + "Parameters: " + DATE_ARGUMENT + "\n"
            + "Example: " + COMMAND_WORD + " " + DATE_ARGUMENT;

    public static final String MESSAGE_SUCCESS = "Sorted %1$d customer(s) by fulfilment date. "
            + "Customers without a date are listed last.";

    /** Orders customers by fulfilment date, with missing dates last, then by name ignoring case. */
    public static final Comparator<Person> DATE_COMPARATOR = Comparator
            .comparing(SortCommand::getSortDate)
            .thenComparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER);

    private static final Logger logger = LogsCenter.getLogger(SortCommand.class);

    /**
     * Returns the fulfilment date of the person's preorder, or {@code LocalDate.MAX} if they have
     * no preorder or no date, so that they are sorted last.
     */
    private static LocalDate getSortDate(Person person) {
        Optional<Preorder> preorder = person.getPreorder();
        if (preorder.isEmpty()) {
            return LocalDate.MAX;
        }
        Optional<FulfilmentDate> date = preorder.get().getDate();
        if (date.isEmpty()) {
            return LocalDate.MAX;
        }
        return date.get().value;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updatePersonListComparator(DATE_COMPARATOR);
        int sortedCount = model.getFilteredPersonList().size();
        logger.info("Sorted " + sortedCount + " customer(s) by fulfilment date");
        return new CommandResult(String.format(MESSAGE_SUCCESS, sortedCount));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof SortCommand;
    }

    @Override
    public int hashCode() {
        return SortCommand.class.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
