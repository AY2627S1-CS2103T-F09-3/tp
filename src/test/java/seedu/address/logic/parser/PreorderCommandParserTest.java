package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULFILMENT_DATE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.PreorderCommand;
import seedu.address.logic.commands.PreorderCommand.EditFulfilmentDescriptor;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.testutil.EditFulfilmentDescriptorBuilder;

public class PreorderCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, PreorderCommand.MESSAGE_USAGE);

    private static final String VALID_DATE = "2026-12-20";
    private static final String VALID_HANDOVER_NOTES = "Call on arrival";

    private final PreorderCommandParser parser = new PreorderCommandParser();

    private void assertParseDescriptor(String userInput, EditFulfilmentDescriptor expectedDescriptor) {
        assertParseSuccess(parser, userInput, new PreorderCommand(INDEX_FIRST_PERSON, expectedDescriptor));
    }

    @Test
    public void parse_eachFieldAlone_success() {
        assertParseDescriptor("1 f/" + VALID_DATE,
                new EditFulfilmentDescriptorBuilder().withDate(VALID_DATE).build());
        assertParseDescriptor("1 m/delivery",
                new EditFulfilmentDescriptorBuilder().withMethod(FulfilmentMethod.DELIVERY).build());
        assertParseDescriptor("1 h/" + VALID_HANDOVER_NOTES,
                new EditFulfilmentDescriptorBuilder().withHandoverNotes(VALID_HANDOVER_NOTES).build());
    }

    @Test
    public void parse_allFieldsInAnyOrder_success() {
        EditFulfilmentDescriptor expectedDescriptor = new EditFulfilmentDescriptorBuilder().withDate(VALID_DATE)
                .withMethod(FulfilmentMethod.DELIVERY).withHandoverNotes(VALID_HANDOVER_NOTES).build();

        assertParseDescriptor("1 f/2026-12-20 m/delivery h/Call on arrival", expectedDescriptor);
        assertParseDescriptor("1 h/Call on arrival f/2026-12-20 m/delivery", expectedDescriptor);
        assertParseDescriptor("  1   m/ DeLiVeRy   h/  Call on arrival   f/ 2026-12-20 ", expectedDescriptor);
    }

    @Test
    public void parse_emptyValue_clearsField() {
        assertParseDescriptor("1 f/", new EditFulfilmentDescriptorBuilder().withoutDate().build());
        assertParseDescriptor("1 h/   ", new EditFulfilmentDescriptorBuilder().withoutHandoverNotes().build());
        assertParseDescriptor("1 f/ m/collection", new EditFulfilmentDescriptorBuilder().withoutDate()
                .withMethod(FulfilmentMethod.COLLECTION).build());
        assertParseDescriptor("1 f/ m/ h/", new EditFulfilmentDescriptorBuilder().withoutDate().withoutMethod()
                .withoutHandoverNotes().build());
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1 f/2026-02-30", FulfilmentDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 f/20-12-2026", FulfilmentDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 m/pickup", FulfilmentMethod.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 h/" + "a".repeat(HandoverNotes.MAX_LENGTH + 1),
                HandoverNotes.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 h/\u2003", HandoverNotes.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_noBreakSpaceBeforeHandoverNotes_trimmed() {
        assertParseDescriptor("1 h/\u00A0Call on arrival",
                new EditFulfilmentDescriptorBuilder().withHandoverNotes("Call on arrival").build());
    }

    @Test
    public void parse_unsupportedPrefixesInConstrainedFields_namesPrefix() {
        for (String field : new String[]{"f/" + VALID_DATE, "m/delivery"}) {
            for (String prefix : new String[]{"x/", "d/", "q/", "F/", "method/"}) {
                assertParseFailure(parser, "1 " + field + " " + prefix + "foo",
                        "Unsupported prefix '" + prefix + "' for preorder.");
            }
            assertParseFailure(parser, "1 " + field + " x/", "Unsupported prefix 'x/' for preorder.");
            assertParseFailure(parser, "1 " + field + " x/foo d/Cake", "Unsupported prefix 'x/' for preorder.");
        }
    }

    @Test
    public void parse_unsupportedPrefixAfterWhitespace_namesPrefix() {
        for (String field : new String[]{"f/" + VALID_DATE, "m/delivery"}) {
            for (String separator : new String[]{"\t", "\n", "\r\n", " \t "}) {
                assertParseFailure(parser, "1 " + field + separator + "x/foo",
                        "Unsupported prefix 'x/' for preorder.");
            }
        }
    }

    @Test
    public void parse_invalidValuesWithoutUnsupportedPrefixes_keepsConstraintMessages() {
        for (String suffix : new String[]{"x/foo", " x /foo", " #04/08", " https://example.com/path"}) {
            assertParseFailure(parser, "1 f/" + VALID_DATE + suffix, FulfilmentDate.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, "1 m/delivery" + suffix, FulfilmentMethod.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, "1 f/x/foo", FulfilmentDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1 m/x/foo", FulfilmentMethod.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_prefixLikeTextInHandoverNotes_preservesTextAndOtherFields() {
        String notes = "Leave w/o ringing d/Cake x/foo";
        EditFulfilmentDescriptor descriptor = new EditFulfilmentDescriptorBuilder().withHandoverNotes(notes)
                .withoutDate().withMethod(FulfilmentMethod.DELIVERY).build();
        assertParseDescriptor("1 h/" + notes + " f/ m/delivery", descriptor);
    }

    @Test
    public void parse_invalidHandoverNotesWithPrefixLikeText_keepsConstraintMessage() {
        assertParseFailure(parser, "1 h/Leave\nx/foo", HandoverNotes.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unsupportedPrefixWithEarlierErrors_preservesValidationOrder() {
        assertParseFailure(parser, "0 f/" + VALID_DATE + " x/foo", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 x/foo f/" + VALID_DATE, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 f/" + VALID_DATE + " x/foo f/2026-12-21",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_FULFILMENT_DATE));
        assertParseFailure(parser, "1 f/2026-02-30 m/delivery x/foo", FulfilmentDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        assertParseFailure(parser, "1 f/2026-12-20 f/2026-12-21",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_FULFILMENT_DATE));
    }

    @Test
    public void parse_noField_failure() {
        assertParseFailure(parser, "1", PreorderCommand.MESSAGE_NOT_CHANGED);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "f/2026-12-20", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "0 f/2026-12-20", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "-1 f/2026-12-20", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "a f/2026-12-20", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 2 f/2026-12-20", MESSAGE_INVALID_FORMAT);

        // d/ is not a prefix of this command, so it is part of the preamble
        assertParseFailure(parser, "1 d/2026-12-20", MESSAGE_INVALID_FORMAT);
    }
}
