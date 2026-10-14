package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_INDEX_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.commands.TagCommand.MESSAGE_INVALID_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.TagCommand;
import seedu.address.model.tag.Tag;

public class TagCommandParserTest {

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
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " t/vip", MESSAGE_INVALID_INDEX_FORMAT);
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 vip", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 /t vip", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, "0 t/vip", MESSAGE_INVALID_INDEX_FORMAT);
        assertParseFailure(parser, "-1 t/vip", MESSAGE_INVALID_INDEX_FORMAT);
        assertParseFailure(parser, "a t/vip", MESSAGE_INVALID_INDEX_FORMAT);
        assertParseFailure(parser, "1.5 t/vip", MESSAGE_INVALID_INDEX_FORMAT);
        assertParseFailure(parser, "2147483648 t/vip", MESSAGE_INVALID_INDEX_FORMAT);
        assertParseFailure(parser, "1 extra t/vip", MESSAGE_INVALID_INDEX_FORMAT);
    }

    @Test
    public void parse_invalidTag_failure() {
        assertParseFailure(parser, "1 t/", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/   ", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/vip 1", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/gluten-free", Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 t/vip p/91234567", Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedTagPrefix_failure() {
        String expectedMessage = Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TAG);
        assertParseFailure(parser, "1 t/vip t/regular", expectedMessage);
        assertParseFailure(parser, "1 t/vip t/vip", expectedMessage);
    }
}
