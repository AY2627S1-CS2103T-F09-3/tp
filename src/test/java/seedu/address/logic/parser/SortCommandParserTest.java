package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SortCommand;

public class SortCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, SortCommand.MESSAGE_USAGE);

    private final SortCommandParser parser = new SortCommandParser();

    @Test
    public void parse_date_success() {
        assertParseSuccess(parser, "date", new SortCommand());
    }

    @Test
    public void parse_dateWithSpacesAndUpperCase_success() {
        assertParseSuccess(parser, "  DATE  ", new SortCommand());
    }

    @Test
    public void parse_emptyArgument_failure() {
        assertParseFailure(parser, "   ", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_unknownSortKey_failure() {
        assertParseFailure(parser, "name", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_extraArgument_failure() {
        assertParseFailure(parser, "date x", MESSAGE_INVALID_FORMAT);
    }
}
