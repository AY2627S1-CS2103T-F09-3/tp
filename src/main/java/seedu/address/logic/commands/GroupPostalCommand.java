package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;

import seedu.address.model.Model;
import seedu.address.model.person.Person;

/** Groups the currently displayed customers by postal code. */
public class GroupPostalCommand extends Command {
    public static final String COMMAND_WORD = "group";
    public static final String POSTAL_ARGUMENT = "postal";
    public static final String MESSAGE_SUCCESS = "Customers grouped by postal code.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        Comparator<Person> comparator = Comparator
                .comparing((Person person) -> person.getPostalCode() == null ? "\uffff"
                        : person.getPostalCode().value)
                .thenComparing(person -> person.getName().fullName);
        model.updatePersonListComparator(comparator);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
