package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.OrderCommand;
import seedu.address.logic.commands.OrderCommand.EditPreorderDescriptor;
import seedu.address.model.preorder.PreorderDetails;
import seedu.address.model.preorder.PreorderStatus;

public class OrderCommandParserTest {
    private final OrderCommandParser parser = new OrderCommandParser();

    @Test
    public void parse_optionalFields_success() {
        assertValid(" 1", null, null);
        assertValid(" 1 d/Cake", "Cake", null);
        assertValid(" 1 s/PENDING", null, PreorderStatus.PENDING);
        assertValid(" 1 d/Cake s/PENDING", "Cake", PreorderStatus.PENDING);
        assertValid(" 1 s/PENDING d/Cake", "Cake", PreorderStatus.PENDING);
    }

    @Test
    public void parse_statusCaseAndWhitespace_normalized() {
        for (PreorderStatus status : PreorderStatus.values()) {
            assertValid(" 1 d/  Cake  s/ " + status.name().toLowerCase(java.util.Locale.ROOT) + "  ",
                    "Cake", status);
        }
        assertValid(" 1 s/pRePaReD", null, PreorderStatus.PREPARED);
    }

    @Test
    public void parse_invalidIndex_failure() {
        for (String input : new String[]{"", "0", "-1", "abc", "1.5", "2147483648", "1 extra"}) {
            assertParseFailure(parser, input + " d/Cake s/PENDING",
                    String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, OrderCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_emptyOrInvalidValues_failure() {
        for (String input : new String[]{" 1 d/", " 1 d/   s/PENDING", " 1 d/Cake\nTarts",
            " 1 d/" + "a".repeat(PreorderDetails.MAX_LENGTH + 1)}) {
            assertParseFailure(parser, input, PreorderDetails.MESSAGE_CONSTRAINTS);
        }
        for (String input : new String[]{" 1 s/", " 1 s/   d/Cake", " 1 s/DONE"}) {
            assertParseFailure(parser, input, PreorderStatus.MESSAGE_CONSTRAINTS);
        }
        assertValid(" 1 d/" + "a".repeat(PreorderDetails.MAX_LENGTH),
                "a".repeat(PreorderDetails.MAX_LENGTH), null);
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        assertParseFailure(parser, " 1 d/Cake d/Tarts", Messages.getErrorMessageForDuplicatePrefixes(
                CliSyntax.PREFIX_DETAILS));
        assertParseFailure(parser, " 1 s/PENDING s/PENDING", Messages.getErrorMessageForDuplicatePrefixes(
                CliSyntax.PREFIX_STATUS));
    }

    @Test
    public void parseCommand_order_dispatchesToOrderParser() throws Exception {
        assertEquals(parser.parse(" 1 d/Cake s/PENDING"),
                new AddressBookParser().parseCommand("order 1 d/Cake s/PENDING"));
    }

    private void assertValid(String input, String details, PreorderStatus status) {
        EditPreorderDescriptor descriptor = new EditPreorderDescriptor();
        if (details != null) {
            descriptor.setDetails(new PreorderDetails(details));
        }
        descriptor.setStatus(status);
        assertParseSuccess(parser, input, new OrderCommand(Index.fromOneBased(1), descriptor));
    }
}
