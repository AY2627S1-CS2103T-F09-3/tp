package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddItemCommand;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.preorder.Remark;
import seedu.address.testutil.ItemBuilder;

/** Tests required fields, optional remarks and validation of additem input. */
public class AddItemCommandParserTest {
    private static final String VALID_FIELDS = " d/Cake q/2 s/pending";
    private static final String USAGE = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT,
            AddItemCommand.MESSAGE_USAGE);
    private final AddItemCommandParser parser = new AddItemCommandParser();

    @Test
    public void parse_requiredFields_success() {
        assertValid("1" + VALID_FIELDS, new ItemBuilder().withName("Cake").build());
    }

    @Test
    public void parse_remarkAndDifferentFieldOrder_success() {
        assertValid("1 r/Less sugar s/prepared q/3 d/Cake",
                new ItemBuilder().withName("Cake").withQuantity("3")
                        .withStatus(ItemStatus.PREPARED).withRemark("Less sugar").build());
    }

    @Test
    public void parse_statusCaseAndWhitespace_normalized() {
        for (ItemStatus status : ItemStatus.values()) {
            assertValid("  1  q/ 2  d/ Cake  s/ " + status.name().toLowerCase(Locale.ROOT) + "  r/ Gift  ",
                    new ItemBuilder().withName("Cake").withStatus(status).withRemark("Gift").build());
        }
        assertValid("1 d/Cake q/2 s/pRePaReD",
                new ItemBuilder().withName("Cake").withStatus(ItemStatus.PREPARED).build());
    }

    @Test
    public void parse_missingRequiredFields_failure() {
        for (String input : new String[]{"1", "1 d/Cake", "1 q/2", "1 s/pending", "1 r/Gift",
            "1 q/2 s/pending", "1 d/Cake s/pending", "1 d/Cake q/2"}) {
            assertParseFailure(parser, input, USAGE);
        }
    }

    @Test
    public void parse_missingOrInvalidCustomerIndex_failure() {
        for (String index : new String[]{"", "0", "-1", "abc", "1.5", "2147483648", "1 extra"}) {
            assertParseFailure(parser, index + VALID_FIELDS, USAGE);
        }
        assertParseFailure(parser, "", USAGE);
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        assertParseFailure(parser, "1" + VALID_FIELDS + " d/Tarts",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DETAILS));
        assertParseFailure(parser, "1" + VALID_FIELDS + " q/3",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_QUANTITY));
        assertParseFailure(parser, "1" + VALID_FIELDS + " s/pending",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_STATUS));
        assertParseFailure(parser, "1" + VALID_FIELDS + " r/Gift r/Gift",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_REMARK));
    }

    @Test
    public void parse_invalidName_failure() {
        for (String name : new String[]{"", "   ", "Cake\nTarts", "Cake\rTarts",
            "a".repeat(ItemName.MAX_LENGTH + 1)}) {
            assertParseFailure(parser, "1 d/" + name + " q/2 s/pending", ItemName.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidQuantity_failure() {
        for (String quantity : new String[]{"", "   ", "0", "-1", "01", "+1", "1.5", "1000", "abc",
            "999999999999"}) {
            assertParseFailure(parser, "1 d/Cake q/" + quantity + " s/pending", Quantity.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidStatus_failure() {
        for (String status : new String[]{"", "   ", "DONE", "pending prepared"}) {
            assertParseFailure(parser, "1 d/Cake q/2 s/" + status, ItemStatus.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unsupportedPrefixesInConstrainedFields_namesPrefix() {
        for (String prefix : new String[]{"x/", "c/", "i/", "Q/", "status/"}) {
            String message = "Unsupported prefix '" + prefix + "' for additem.";
            assertParseFailure(parser, "1 d/Cake q/2 " + prefix + "foo s/pending", message);
            assertParseFailure(parser, "1 d/Cake q/2 s/pending " + prefix + "foo", message);
        }
        assertParseFailure(parser, "1 d/Cake q/2 x/ s/pending", "Unsupported prefix 'x/' for additem.");
        assertParseFailure(parser, "1 d/Cake q/2 s/pending x/", "Unsupported prefix 'x/' for additem.");
        assertParseFailure(parser, "1 d/Cake q/2 x/foo c/012345 s/pending",
                "Unsupported prefix 'x/' for additem.");
        assertParseFailure(parser, "1 d/Cake q/2 s/pending x/foo c/012345",
                "Unsupported prefix 'x/' for additem.");
    }

    @Test
    public void parse_unsupportedPrefixAfterWhitespace_namesPrefix() {
        for (String separator : new String[]{"\t", "\n", "\r\n", " \t "}) {
            assertParseFailure(parser, "1 d/Cake q/2" + separator + "x/foo s/pending",
                    "Unsupported prefix 'x/' for additem.");
            assertParseFailure(parser, "1 d/Cake q/2 s/pending" + separator + "x/foo",
                    "Unsupported prefix 'x/' for additem.");
        }
    }

    @Test
    public void parse_invalidValuesWithoutUnsupportedPrefixes_keepsConstraintMessages() {
        for (String suffix : new String[]{"x/foo", " x /foo", " #04/08", " https://example.com/path"}) {
            assertParseFailure(parser, "1 d/Cake q/2" + suffix + " s/pending", Quantity.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "1 d/Cake q/2 s/pending" + suffix, ItemStatus.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, "1 d/Cake q/x/foo s/pending", Quantity.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 d/Cake q/2 s/x/foo", ItemStatus.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_prefixLikeTextInNameAndRemark_preservesText() {
        String name = "Cake w/o nuts c/012345 x/foo";
        String remark = "Pack w/o nuts i/1 x/foo";
        assertValid("1 d/" + name + " q/2 r/" + remark + " s/prepared",
                new ItemBuilder().withName(name).withRemark(remark).withStatus(ItemStatus.PREPARED).build());
    }

    @Test
    public void parse_invalidFreeTextWithPrefixLikeText_keepsConstraintMessages() {
        assertParseFailure(parser, "1 d/Cake\nx/foo q/2 s/pending", ItemName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 d/Cake q/2 s/pending r/Pack\nx/foo", Remark.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unsupportedPrefixWithEarlierErrors_preservesValidationOrder() {
        assertParseFailure(parser, "0 d/Cake q/2 x/foo s/pending", USAGE);
        assertParseFailure(parser, "1 x/foo d/Cake q/2 s/pending", USAGE);
        assertParseFailure(parser, "1 q/2 x/foo s/pending", USAGE);
        assertParseFailure(parser, "1 d/Cake q/2 x/foo q/3 s/pending",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_QUANTITY));
        assertParseFailure(parser, "1 d/ q/2 x/foo s/pending", ItemName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 d/Cake q/0 s/pending x/foo", Quantity.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_emptyOrInvalidRemark_failure() {
        for (String remark : new String[]{"", "   ", "Less\nsugar", "Less\rsugar",
            "a".repeat(Remark.MAX_LENGTH + 1)}) {
            assertParseFailure(parser, "1" + VALID_FIELDS + " r/" + remark, Remark.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_validBoundaries_success() {
        for (String quantity : new String[]{"1", "999"}) {
            String name = "a".repeat(ItemName.MAX_LENGTH);
            String remark = "a".repeat(Remark.MAX_LENGTH);
            assertValid("1 d/" + name + " q/" + quantity + " s/pending r/" + remark,
                    new ItemBuilder().withName(name).withQuantity(quantity).withRemark(remark).build());
        }
        assertValid("1 d/A q/1 s/pending r/B",
                new ItemBuilder().withName("A").withQuantity("1").withRemark("B").build());
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parseCommand_additem_dispatchesToAddItemParser() throws Exception {
        assertEquals(parser.parse("1" + VALID_FIELDS),
                new AddressBookParser().parseCommand("additem 1" + VALID_FIELDS));
    }

    @Test
    public void parse_multipleErrors_reportsValidationOrder() {
        assertParseFailure(parser, "0 d/Cake d/Tarts", USAGE);
        assertParseFailure(parser, "1 d/Cake d/Tarts",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DETAILS));
        assertParseFailure(parser, "1 d/ q/0 s/invalid r/" + "a".repeat(Remark.MAX_LENGTH + 1),
                ItemName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 d/Cake q/0 s/invalid", Quantity.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 d/Cake q/1 s/invalid r/" + "a".repeat(Remark.MAX_LENGTH + 1),
                ItemStatus.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_secondCustomer_preservesCustomerIndex() {
        Item item = new ItemBuilder().withName("Cake").withRemark("Gift box").build();
        assertParseSuccess(parser, "2" + VALID_FIELDS + " r/Gift box",
                new AddItemCommand(Index.fromOneBased(2), item));
    }

    @Test
    public void parse_remarkOmittedAfterRemark_suppliesIndependentItem() {
        Item withRemark = new ItemBuilder().withName("Cake").withRemark("Gift box").build();
        assertValid("1" + VALID_FIELDS + " r/Gift box", withRemark);
        assertValid("1" + VALID_FIELDS, new ItemBuilder().withName("Cake").build());
    }

    private void assertValid(String input, Item item) {
        assertParseSuccess(parser, input, new AddItemCommand(Index.fromOneBased(1), item));
    }
}
