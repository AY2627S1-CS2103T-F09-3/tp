package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.DeleteItemCommand;

public class DeleteItemCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteItemCommand.MESSAGE_USAGE);

    private DeleteItemCommandParser parser = new DeleteItemCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteItemCommand() {
        assertParseSuccess(parser, "1 i/1", new DeleteItemCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_validArgsWithSurroundingWhitespace_returnsDeleteItemCommand() {
        assertParseSuccess(parser, "  1  i/  1  ", new DeleteItemCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_emptyArgs_throwsParseException() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_zeroIndex_throwsParseException() {
        assertParseFailure(parser, "0 i/1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_negativeIndex_throwsParseException() {
        assertParseFailure(parser, "-1 i/1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_nonNumericIndex_throwsParseException() {
        assertParseFailure(parser, "a i/1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_extraArgs_throwsParseException() {
        assertParseFailure(parser, "1 2 i/1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_unexpectedPrefix_throwsParseException() {
        assertParseFailure(parser, "1 d/cake", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_overflowingCustomerIndex_success() {
        assertParseSuccess(parser, "2147483648 i/1",
                new DeleteItemCommand(Index.fromZeroBased(Integer.MAX_VALUE), Index.fromOneBased(1)));
    }

    @Test
    public void parse_overflowingItemIndex_success() {
        assertParseSuccess(parser, "1 i/2147483648",
                new DeleteItemCommand(Index.fromOneBased(1), Index.fromZeroBased(Integer.MAX_VALUE)));
    }

    @Test
    public void parse_distinctIndices_returnsDeleteItemCommand() {
        assertParseSuccess(parser, "2 i/3",
                new DeleteItemCommand(Index.fromOneBased(2), Index.fromOneBased(3)));
    }

    @Test
    public void parse_missingItemIndex_showsUsage() {
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidItemIndex_showsIndexConstraint() {
        for (String value : new String[]{"", "   ", "0", "-1", "+1", "1.5", "abc", "1 extra"}) {
            assertParseFailure(parser, "1 i/" + value, Messages.MESSAGE_INVALID_ITEM_INDEX);
        }
    }

    @Test
    public void parse_missingOrMalformedCustomerIndex_throwsParseException() {
        for (String value : new String[]{"", "+1", "1.5"}) {
            assertParseFailure(parser, value + " i/1", MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_duplicateItemIndex_throwsParseException() {
        String message = Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ITEM_INDEX);
        assertParseFailure(parser, "1 i/1 i/1", message);
        assertParseFailure(parser, "1 i/1 i/2", message);
        assertParseFailure(parser, "1 i/ i/", message);
        assertParseFailure(parser, "0 i/1 i/2", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_extraFieldOrOldPrefix_throwsParseException() {
        assertParseFailure(parser, "1 i/1 q/2", Messages.MESSAGE_INVALID_ITEM_INDEX);
        assertParseFailure(parser, "1 o/1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
