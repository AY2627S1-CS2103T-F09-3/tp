package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_POSTAL_CODE;

import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.PostalCode;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                        PREFIX_POSTAL_CODE);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_ADDRESS, PREFIX_PHONE, PREFIX_EMAIL,
                PREFIX_POSTAL_CODE)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_POSTAL_CODE);
        String nameValue = argMultimap.getValue(PREFIX_NAME).get();
        Name name;
        try {
            name = ParserUtil.parseName(nameValue);
        } catch (ParseException pe) {
            throw createFieldParseException(nameValue, pe);
        }

        String phoneValue = argMultimap.getValue(PREFIX_PHONE).get();
        Phone phone;
        try {
            phone = ParserUtil.parsePhone(phoneValue);
        } catch (ParseException pe) {
            throw createFieldParseException(phoneValue, pe);
        }

        String emailValue = argMultimap.getValue(PREFIX_EMAIL).get();
        Email email;
        try {
            email = ParserUtil.parseEmail(emailValue);
        } catch (ParseException pe) {
            throw createFieldParseException(emailValue, pe);
        }

        Address address = ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get());
        String postalCodeValue = argMultimap.getValue(PREFIX_POSTAL_CODE).get();
        PostalCode postalCode;
        try {
            postalCode = ParserUtil.parsePostalCode(postalCodeValue);
        } catch (ParseException pe) {
            throw createFieldParseException(postalCodeValue, pe);
        }

        Person person = new Person(name, phone, email, address, postalCode, Set.of());

        return new AddCommand(person);
    }

    private ParseException createFieldParseException(String value, ParseException cause) {
        return ParserUtil.createFieldParseException(value, cause, AddCommand.COMMAND_WORD,
                PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_POSTAL_CODE);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
