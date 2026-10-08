package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.logic.parser.UntagCommandParser.MESSAGE_INVALID_FORMAT;
import static seedu.address.logic.parser.UntagCommandParser.MESSAGE_INVALID_INDEX;
import static seedu.address.logic.parser.UntagCommandParser.MESSAGE_INVALID_TAG;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.UntagCommand;
import seedu.address.model.tag.Tag;

public class UntagCommandParserTest {

    private final UntagCommandParser parser = new UntagCommandParser();

    @Test
    public void parse_validArgs_success() {
        UntagCommand expectedCommand = new UntagCommand(INDEX_FIRST_PERSON, new Tag("VIP123"));
        assertParseSuccess(parser, "1 t/VIP123", expectedCommand);
        assertParseSuccess(parser, "  1   t/  VIP123  ", expectedCommand);
    }

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " t/friends", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 friends", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 /t friends", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, "0 t/friends", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "-1 t/friends", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "a t/friends", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1.5 t/friends", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "2147483648 t/friends", MESSAGE_INVALID_INDEX);
        assertParseFailure(parser, "1 extra t/friends", MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_invalidTag_failure() {
        assertParseFailure(parser, "1 t/", MESSAGE_INVALID_TAG);
        assertParseFailure(parser, "1 t/   ", MESSAGE_INVALID_TAG);
        assertParseFailure(parser, "1 t/close friends", MESSAGE_INVALID_TAG);
        assertParseFailure(parser, "1 t/friends!", MESSAGE_INVALID_TAG);
        assertParseFailure(parser, "1 t/friends p/91234567", MESSAGE_INVALID_TAG);
    }

    @Test
    public void parse_repeatedTagPrefix_failure() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TAG);
        assertParseFailure(parser, "1 t/friends t/VIP", expectedMessage);
        assertParseFailure(parser, "1 t/friends t/friends", expectedMessage);
    }
}
