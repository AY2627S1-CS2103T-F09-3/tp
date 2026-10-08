package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.TagCommand;
import seedu.address.model.tag.Tag;

public class TagCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE);

    private TagCommandParser parser = new TagCommandParser();

    @Test
    public void parse_validArgs_success() {
        TagCommand expectedCommand = new TagCommand(INDEX_FIRST_PERSON, new Tag(VALID_TAG_HUSBAND));

        assertParseSuccess(parser, "1" + TAG_DESC_HUSBAND, expectedCommand);

        // leading and trailing whitespace
        assertParseSuccess(parser, "  1  " + TAG_DESC_HUSBAND + "  ", expectedCommand);
    }

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, TAG_DESC_HUSBAND, MESSAGE_INVALID_FORMAT);

        // no tag specified
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);

        // no index and no tag specified
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // zero index
        assertParseFailure(parser, "0" + TAG_DESC_HUSBAND, MESSAGE_INVALID_FORMAT);

        // negative index
        assertParseFailure(parser, "-5" + TAG_DESC_HUSBAND, MESSAGE_INVALID_FORMAT);

        // non-numeric index
        assertParseFailure(parser, "a" + TAG_DESC_HUSBAND, MESSAGE_INVALID_FORMAT);

        // extra words in preamble
        assertParseFailure(parser, "1 abc" + TAG_DESC_HUSBAND, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidTag_failure() {
        // non-alphanumeric tag
        assertParseFailure(parser, "1" + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS);

        // empty tag
        assertParseFailure(parser, "1 " + PREFIX_TAG, Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedTagPrefix_failure() {
        assertParseFailure(parser, "1" + TAG_DESC_HUSBAND + TAG_DESC_FRIEND,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TAG));
    }
}
