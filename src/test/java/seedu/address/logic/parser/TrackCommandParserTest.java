package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.model.preorder.ItemStatus.PENDING;
import static seedu.address.model.preorder.ItemStatus.PREPARED;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.TrackCommand;
import seedu.address.model.preorder.ItemStatus;

public class TrackCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, TrackCommand.MESSAGE_USAGE);
    private static final String MESSAGE_DUPLICATE_STATUS =
            "Multiple values specified for the following single-valued field(s): s/";

    private final TrackCommandParser parser = new TrackCommandParser();

    @Test
    public void parse_nullArgs_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parse_noArguments_returnsActiveOrderCommand() {
        TrackCommand expectedCommand = new TrackCommand(Optional.empty());
        assertParseSuccess(parser, "", expectedCommand);
        assertParseSuccess(parser, "   ", expectedCommand);
        assertParseSuccess(parser, "\t  \r  \n", expectedCommand);
    }

    @Test
    public void parse_validStatuses_returnsStatusCommand() {
        for (ItemStatus status : ItemStatus.values()) {
            TrackCommand expectedCommand = new TrackCommand(Optional.of(status));
            assertParseSuccess(parser, "s/" + status, expectedCommand);
            assertParseSuccess(parser, "s/" + status.name().toLowerCase(Locale.ROOT), expectedCommand);
        }
        assertParseSuccess(parser, "s/pEnDiNg", new TrackCommand(Optional.of(PENDING)));
        assertParseSuccess(parser, "s/Prepared", new TrackCommand(Optional.of(PREPARED)));
    }

    @Test
    public void parse_surroundingWhitespace_returnsStatusCommand() {
        TrackCommand expectedCommand = new TrackCommand(Optional.of(PENDING));
        assertParseSuccess(parser, "  s/  pending  ", expectedCommand);
        assertParseSuccess(parser, "\t s/\tPending\t ", expectedCommand);
    }

    @Test
    public void parse_missingStatusValue_failure() {
        assertParseFailure(parser, "s/", ItemStatus.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "  s/  ", ItemStatus.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "s/\t", ItemStatus.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_invalidStatus_failure() {
        String[] invalidStatuses = {"DONE", "COLLECTED", "COMPLETE", "PEND ING", "PENDING,PREPARED", "*"};
        for (String status : invalidStatuses) {
            assertParseFailure(parser, "s/" + status, ItemStatus.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_bareStatus_failure() {
        assertParseFailure(parser, "PENDING", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "pending", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 s/PENDING", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "extra s/PENDING", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_incorrectStatusPrefix_failure() {
        String[] invalidArguments = {"S/PENDING", "status/PENDING", "s:PENDING", "/s PENDING", "s PENDING"};
        for (String args : invalidArguments) {
            assertParseFailure(parser, args, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_unsupportedParameter_failure() {
        String[] unsupportedParameters = {"d/cakes", "n/Alex", "t/vip", "x/PENDING", "--status PENDING"};
        for (String parameter : unsupportedParameters) {
            assertParseFailure(parser, parameter, MESSAGE_INVALID_FORMAT);
            assertParseFailure(parser, parameter + " s/PENDING", MESSAGE_INVALID_FORMAT);
            assertParseFailure(parser, "s/PENDING " + parameter, ItemStatus.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_extraTextAfterStatus_failure() {
        assertParseFailure(parser, "s/PENDING extra", ItemStatus.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "s/PENDING PREPARED", ItemStatus.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedStatusPrefix_failure() {
        assertParseFailure(parser, "s/PENDING s/PREPARED", MESSAGE_DUPLICATE_STATUS);
        assertParseFailure(parser, "s/PENDING s/PENDING", MESSAGE_DUPLICATE_STATUS);
        assertParseFailure(parser, "s/ s/PENDING", MESSAGE_DUPLICATE_STATUS);
        assertParseFailure(parser, "s/PENDING s/", MESSAGE_DUPLICATE_STATUS);
        assertParseFailure(parser, "s/DONE s/PENDING", MESSAGE_DUPLICATE_STATUS);
        assertParseFailure(parser, "s/PENDING s/PREPARED s/COMPLETED", MESSAGE_DUPLICATE_STATUS);
    }
}
