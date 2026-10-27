package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditItemCommand;
import seedu.address.logic.commands.EditItemCommand.EditItemDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.preorder.Remark;

public class EditItemCommandParserTest {
    private final EditItemCommandParser parser = new EditItemCommandParser();

    @Test
    public void parse_optionalFields_success() {

        assertValid(" 1 i/1 d/Cake", "Cake", null);
        assertValid(" 1 i/1 s/PENDING", null, ItemStatus.PENDING);
        assertValid(" 1 i/1 d/Cake s/PENDING", "Cake", ItemStatus.PENDING);
        assertValid(" 1 i/1 s/PENDING d/Cake", "Cake", ItemStatus.PENDING);
    }

    @Test
    public void parse_statusCaseAndWhitespace_normalized() {
        for (ItemStatus status : ItemStatus.values()) {
            assertValid(" 1 i/1 d/  Cake  s/ " + status.name().toLowerCase(java.util.Locale.ROOT) + "  ",
                    "Cake", status);
        }
        assertValid(" 1 i/1 s/pRePaReD", null, ItemStatus.PREPARED);
    }

    @Test
    public void parse_invalidIndex_failure() {
        for (String input : new String[]{"", "0", "-1", "abc", "1.5", "2147483648", "1 extra"}) {
            assertParseFailure(parser, input + " i/1 d/Cake s/PENDING",
                    String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditItemCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_emptyOrInvalidValues_failure() {
        for (String input : new String[]{" 1 i/1 d/", " 1 i/1 d/   s/PENDING", " 1 i/1 d/\u2003",
            " 1 i/1 d/Cake\nTarts", " 1 i/1 d/" + "a".repeat(ItemName.MAX_LENGTH + 1)}) {
            assertParseFailure(parser, input, ItemName.MESSAGE_CONSTRAINTS);
        }
        for (String input : new String[]{" 1 i/1 s/", " 1 i/1 s/   d/Cake", " 1 i/1 s/DONE"}) {
            assertParseFailure(parser, input, ItemStatus.MESSAGE_CONSTRAINTS);
        }
        assertValid(" 1 i/1 d/" + "a".repeat(ItemName.MAX_LENGTH),
                "a".repeat(ItemName.MAX_LENGTH), null);
    }

    @Test
    public void parse_duplicatePrefixes_failure() {
        assertParseFailure(parser, " 1 i/1 d/Cake d/Tarts", Messages.getErrorMessageForDuplicatePrefixes(
                CliSyntax.PREFIX_DETAILS));
        assertParseFailure(parser, " 1 i/1 s/PENDING s/PENDING", Messages.getErrorMessageForDuplicatePrefixes(
                CliSyntax.PREFIX_STATUS));
    }

    @Test
    public void parseCommand_edititem_dispatchesToEditItemParser() throws Exception {
        assertEquals(parser.parse(" 1 i/1 d/Cake s/PENDING"),
                new AddressBookParser().parseCommand("edititem 1 i/1 d/Cake s/PENDING"));
    }

    @Test
    public void parse_missingItemIndex_showsUsage() {
        String usage = String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditItemCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1 d/Cake", usage);
    }

    @Test
    public void parse_invalidItemIndex_showsIndexConstraint() {
        for (String value : new String[]{"", "   ", "0", "-1", "+1", "abc", "1.5", "2147483648", "1 extra"}) {
            assertParseFailure(parser, "1 i/" + value + " d/Cake", Messages.MESSAGE_INVALID_ITEM_INDEX);
            assertParseFailure(parser, "1 i/" + value, Messages.MESSAGE_INVALID_ITEM_INDEX);
        }
    }

    @Test
    public void parse_noEditFields_showsNoChangesMessage() {
        assertParseFailure(parser, "1 i/1", EditItemCommand.MESSAGE_NOT_EDITED);
    }

    @Test
    public void parse_quantityBoundariesAndRemarks_success() {
        for (String quantity : new String[]{"1", "999"}) {
            EditItemDescriptor descriptor = new EditItemDescriptor();
            descriptor.setQuantity(new Quantity(quantity));
            descriptor.setRemark(Optional.of(new Remark("a".repeat(Remark.MAX_LENGTH))));
            assertParseSuccess(parser, " 1 r/ " + "a".repeat(Remark.MAX_LENGTH) + " q/ " + quantity + " i/1 ",
                    new EditItemCommand(Index.fromOneBased(1), Index.fromOneBased(1), descriptor));
        }
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setRemark(Optional.empty());
        assertParseSuccess(parser, "1 r/   i/1", new EditItemCommand(
                Index.fromOneBased(1), Index.fromOneBased(1), descriptor));
    }

    @Test
    public void parse_invalidQuantityOrRemark_failure() {
        for (String quantity : new String[]{"", "0", "-1", "01", "1.5", "1000", "abc", "999999999999"}) {
            assertParseFailure(parser, "1 i/1 q/" + quantity, Quantity.MESSAGE_CONSTRAINTS);
        }
        for (String remark : new String[]{"a".repeat(Remark.MAX_LENGTH + 1), "Less\nsugar"}) {
            assertParseFailure(parser, "1 i/1 r/" + remark, Remark.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_duplicateItemFields_failure() {
        assertParseFailure(parser, "1 i/1 i/2 q/1",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_ITEM_INDEX));
        assertParseFailure(parser, "1 i/1 q/1 q/2",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_QUANTITY));
        assertParseFailure(parser, "1 i/1 r/ r/Gift",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_REMARK));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parseCommand_oldOrderName_rejected() {
        AddressBookParser commandParser = new AddressBookParser();
        assertThrows(ParseException.class, () -> commandParser.parseCommand("order 1 d/Cake s/pending"));
    }

    @Test
    public void parse_multipleErrors_reportsValidationOrder() {
        assertParseFailure(parser, "0 d/Cake d/Tarts",
                String.format(Messages.MESSAGE_INVALID_COMMAND_FORMAT, EditItemCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1 d/Cake d/Tarts q/2 s/pending",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_DETAILS));
        assertParseFailure(parser, "1 i/1 d/ q/0 s/invalid r/" + "a".repeat(Remark.MAX_LENGTH + 1),
                ItemName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 i/1 d/Cake q/0 s/invalid", Quantity.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 i/1 d/Cake q/1 s/invalid r/" + "a".repeat(Remark.MAX_LENGTH + 1),
                ItemStatus.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsWithDistinctIndices_preservesEveryValue() {
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setName(new ItemName("Chocolate Cake"));
        descriptor.setQuantity(new Quantity("3"));
        descriptor.setStatus(ItemStatus.PREPARED);
        descriptor.setRemark(Optional.of(new Remark("Gift box")));
        assertParseSuccess(parser, "2 r/Gift box s/prepared q/3 i/4 d/Chocolate Cake",
                new EditItemCommand(Index.fromOneBased(2), Index.fromOneBased(4), descriptor));
    }

    @Test
    public void parse_quantityOnly_leavesOtherFieldsUnspecified() {
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setQuantity(new Quantity("3"));
        assertParseSuccess(parser, "1 i/2 q/3",
                new EditItemCommand(Index.fromOneBased(1), Index.fromOneBased(2), descriptor));
    }

    @Test
    public void parse_remarkOnly_distinguishesReplacementRemovalAndOmission() {
        Index first = Index.fromOneBased(1);
        EditItemDescriptor descriptor = new EditItemDescriptor();
        descriptor.setRemark(Optional.of(new Remark("Gift box")));
        assertParseSuccess(parser, "1 i/1 r/Gift box", new EditItemCommand(first, first, descriptor));
        descriptor.setRemark(Optional.empty());
        assertParseSuccess(parser, "1 i/1 r/", new EditItemCommand(first, first, descriptor));
        assertParseFailure(parser, "1 i/1", EditItemCommand.MESSAGE_NOT_EDITED);
        assertValid("1 i/1 d/Cake", "Cake", null);
    }

    private void assertValid(String input, String name, ItemStatus status) {
        EditItemDescriptor descriptor = new EditItemDescriptor();
        if (name != null) {
            descriptor.setName(new ItemName(name));
        }
        descriptor.setStatus(status);
        assertParseSuccess(parser, input,
                new EditItemCommand(Index.fromOneBased(1), Index.fromOneBased(1), descriptor));
    }
}
