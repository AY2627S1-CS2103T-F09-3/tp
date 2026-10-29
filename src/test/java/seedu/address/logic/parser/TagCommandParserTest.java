package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.TagCommand;
import seedu.address.model.tag.Tag;

public class TagCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE);

    private final TagCommandParser parser = new TagCommandParser();

    @Test
    public void parse_validArgs_success() {
        TagCommand expectedCommand = new TagCommand(INDEX_FIRST_PERSON, new Tag("VIP123"));
        assertParseSuccess(parser, "1 t/VIP123", expectedCommand);
        assertParseSuccess(parser, "  1   t/  VIP123  ", expectedCommand);
    }

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " t/regular", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 regular", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 /t regular", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, "0 t/regular", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "-1 t/regular", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "a t/regular", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1.5 t/regular", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 extra t/regular", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_overflowingIndex_success() {
        assertParseSuccess(parser, "2147483648 t/regular",
                new TagCommand(Index.fromZeroBased(Integer.MAX_VALUE), new Tag("regular")));
    }

    @Test
    public void parse_invalidIndexAndTag_reportsIndexErrorFirst() {
        assertParseFailure(parser, "0 t/!", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "0 t/regular t/VIP", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidTag_failure() {
        assertParseFailure(parser, "1 t/", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/   ", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/gluten free", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/gluten-free", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unsupportedPrefixAfterTag_namesPrefix() {
        for (String prefix : new String[]{"x/", "p/", "n/", "T/", "status/", "w/"}) {
            assertParseFailure(parser, "1 t/VIP " + prefix + "foo",
                    "Unsupported prefix '" + prefix + "' for tag.");
        }
        assertParseFailure(parser, "1 t/VIP x/", "Unsupported prefix 'x/' for tag.");
        assertParseFailure(parser, "1 t/VIP x/2 p/91234567", "Unsupported prefix 'x/' for tag.");
        assertParseFailure(parser, "1 t/regular p/91234567", "Unsupported prefix 'p/' for tag.");
    }

    @Test
    public void parse_unsupportedPrefixAfterWhitespace_namesPrefix() {
        for (String separator : new String[]{"\t", "\n", "\r\n", " \t "}) {
            assertParseFailure(parser, "1 t/VIP" + separator + "x/foo", "Unsupported prefix 'x/' for tag.");
        }
    }

    @Test
    public void parse_invalidValuesWithoutUnsupportedPrefixes_keepsConstraintMessage() {
        for (String value : new String[]{"VIPx/foo", "x/foo", "VIP x /foo", "VIP #04/08",
            "VIP https://example.com/path", "VIP 2x/foo"}) {
            assertParseFailure(parser, "1 t/" + value, Tag.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unsupportedPrefixWithEarlierErrors_preservesValidationOrder() {
        assertParseFailure(parser, "0 t/VIP x/foo", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 x/foo t/VIP", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 x/foo", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 p/91234567", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "0 t/VIP x/foo t/friends", MESSAGE_INVALID_FORMAT);
        String duplicateMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TAG);
        assertParseFailure(parser, "1 t/VIP x/foo t/friends", duplicateMessage);
        assertParseFailure(parser, "1 t/VIP t/friends x/foo", duplicateMessage);
    }

    @Test
    public void parse_tagLengthBoundaries_checksTrimmedLength() {
        for (int length : new int[]{29, 30}) {
            String tag = "A".repeat(length);
            assertParseSuccess(parser, "1 t/  " + tag + "  ",
                    new TagCommand(INDEX_FIRST_PERSON, new Tag(tag)));
        }
        assertParseFailure(parser, "1 t/  " + "A".repeat(31) + "  ", Tag.MESSAGE_LENGTH_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedTagPrefix_failure() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TAG);
        assertParseFailure(parser, "1 t/regular t/VIP", expectedMessage);
        assertParseFailure(parser, "1 t/regular t/regular", expectedMessage);
    }
}
