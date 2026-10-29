package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.Model.SEARCH_CRITERIA_SHOW_ALL_PERSONS;

import seedu.address.model.AddressBook;
import seedu.address.model.Model;

/**
 * Clears the address book.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String CONFIRMATION_INPUT = "confirm";
    public static final String MESSAGE_CONFIRMATION_REQUIRED = "This will permanently delete all customers and their "
            + "preorder data. Type 'confirm' to proceed; any other input cancels and is discarded.";
    public static final String MESSAGE_CONFIRMATION_CANCELLED = "Clear cancelled. No data was deleted. Any typed input "
            + "was discarded; enter it again if needed.";
    public static final String MESSAGE_SUCCESS = "All customers have been cleared!";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setAddressBook(new AddressBook());
        model.updateFilteredPersonList(SEARCH_CRITERIA_SHOW_ALL_PERSONS);
        model.updatePersonListComparator(null);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
