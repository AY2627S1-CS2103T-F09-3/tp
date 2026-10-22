package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Groups the currently displayed customers by postal code. */
public class GroupPostalCommand extends Command {
    public static final String COMMAND_WORD = "group";
    public static final String POSTAL_ARGUMENT = "postal";
    public static final String MESSAGE_SUCCESS = "Customers grouped by postal code.";

    /** Orders customers by postal prefix, then full postal code, then name, with missing codes last. */
    public static final Comparator<Person> POSTAL_COMPARATOR = Comparator
            .comparing(GroupPostalCommand::getPostalGroupKey)
            .thenComparing(person -> person.getPostalCode() == null
                    ? "" : person.getPostalCode().value)
            .thenComparing(person -> person.getName().fullName);

    private static final String MISSING_POSTAL_CODE = "\uffff";
    private static final Logger logger = LogsCenter.getLogger(GroupPostalCommand.class);

    /** Returns the first two digits of a person's postal code, or the missing-code key. */
    public static String getPostalGroupKey(Person person) {
        return person.getPostalCode() == null
                ? MISSING_POSTAL_CODE : person.getPostalCode().value.substring(0, 2);
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updatePersonListComparator(POSTAL_COMPARATOR);
        logger.info(MESSAGE_SUCCESS);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
