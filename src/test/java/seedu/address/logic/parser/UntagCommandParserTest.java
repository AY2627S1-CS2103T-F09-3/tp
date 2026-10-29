package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.UntagCommand;
import seedu.address.model.tag.Tag;

public class UntagCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE);

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
        assertParseFailure(parser, " t/friends", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 friends", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 /t friends", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, "0 t/friends", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "-1 t/friends", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "a t/friends", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1.5 t/friends", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "2147483648 t/friends", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 extra t/friends", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndexAndTag_reportsIndexErrorFirst() {
        assertParseFailure(parser, "0 t/!", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "0 t/friends t/VIP", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidTag_failure() {
        assertParseFailure(parser, "1 t/", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/   ", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/close friends", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/friends!", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/friends p/91234567", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_tagLengthBoundaries_checksTrimmedLength() {
        for (int length : new int[]{29, 30}) {
            String tag = "A".repeat(length);
            assertParseSuccess(parser, "1 t/  " + tag + "  ",
                    new UntagCommand(INDEX_FIRST_PERSON, new Tag(tag)));
        }
        assertParseFailure(parser, "1 t/  " + "A".repeat(31) + "  ", Tag.MESSAGE_LENGTH_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedTagPrefix_failure() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TAG);
        assertParseFailure(parser, "1 t/friends t/VIP", expectedMessage);
        assertParseFailure(parser, "1 t/friends t/friends", expectedMessage);
    }
}
