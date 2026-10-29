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
