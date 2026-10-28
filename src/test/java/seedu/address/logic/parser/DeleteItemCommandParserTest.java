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
    public void parse_indexOverflow_throwsParseException() {
        assertParseFailure(parser, "2147483648 i/1", MESSAGE_INVALID_FORMAT);
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
        for (String value : new String[]{"", "   ", "0", "-1", "+1", "1.5", "abc", "2147483648", "1 extra",
            "1x/2", "x/2", "1 x /2", "1 #04/08"}) {
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
        assertParseFailure(parser, "1 i/1 q/2", "Unsupported prefix 'q/' for deleteitem.");
        assertParseFailure(parser, "1 o/1", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_unsupportedPrefixAfterItemIndex_namesPrefix() {
        for (String prefix : new String[]{"x/", "c/", "d/", "Q/", "status/"}) {
            assertParseFailure(parser, "1 i/1 " + prefix + "2",
                    "Unsupported prefix '" + prefix + "' for deleteitem.");
        }
        assertParseFailure(parser, "1 i/1 x/2 q/3", "Unsupported prefix 'x/' for deleteitem.");
        assertParseFailure(parser, "1 i/1 x/", "Unsupported prefix 'x/' for deleteitem.");
    }

    @Test
    public void parse_unsupportedPrefixAfterWhitespace_namesPrefix() {
        for (String separator : new String[]{"\t", "\n", "\r\n", " \t "}) {
            assertParseFailure(parser, "1 i/1" + separator + "x/2", "Unsupported prefix 'x/' for deleteitem.");
        }
    }

    @Test
    public void parse_unsupportedPrefixWithEarlierErrors_preservesValidationOrder() {
        assertParseFailure(parser, "0 i/1 x/2", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 x/2 i/1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 i/1 x/2 i/2",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ITEM_INDEX));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
