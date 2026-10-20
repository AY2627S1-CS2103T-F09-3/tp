package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    public void parse_missingOrExtraArguments_failure() {
        for (String args : new String[]{"", "POSTAL", "postal extra", "postal c/012345", "other"}) {
            assertThrows(ParseException.class, () -> new GroupPostalParser().parse(args));
        }
    }
}
