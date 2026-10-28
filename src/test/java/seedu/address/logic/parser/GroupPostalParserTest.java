package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.GroupPostalCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Checks the complete group-postal syntax and command routing. */
public class GroupPostalParserTest {

    @Test
    public void parse_postalWithWhitespace_success() throws Exception {
        assertInstanceOf(GroupPostalCommand.class, new GroupPostalParser().parse("  postal  "));
        assertInstanceOf(GroupPostalCommand.class, new AddressBookParser().parseCommand("  group postal  "));
    }

    @Test
    public void parse_postalWithDifferentCapitalisations_success() throws Exception {
        for (String args : new String[]{"postal", "POSTAL", "Postal", "pOsTaL", "\tPoStAl\t"}) {
            assertInstanceOf(GroupPostalCommand.class, new GroupPostalParser().parse(args));
            assertInstanceOf(GroupPostalCommand.class, new AddressBookParser().parseCommand("  group " + args + "  "));
        }
    }

    @Test
    public void parse_missingOrExtraArguments_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                "group postal: Groups customers by postal code.\n"
                        + "Parameters: postal\n"
                        + "Example: group postal");
        AddressBookParser commandParser = new AddressBookParser();
        for (String args : new String[]{"", " \t ", "postal extra", "POSTAL extra", "postal c/012345", "other"}) {
            assertThrows(ParseException.class, expectedMessage, () -> new GroupPostalParser().parse(args));
            assertThrows(ParseException.class, expectedMessage, () -> commandParser.parseCommand("group " + args));
        }
    }
}
