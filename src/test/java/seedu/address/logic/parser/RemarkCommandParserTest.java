package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

public class RemarkCommandParserTest {
    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_validArgs_returnsRemarkCommand() {
        assertParseSuccess(parser, "2 r/Likes baseball",
                new RemarkCommand(INDEX_SECOND_PERSON, new Remark("Likes baseball")));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "a r/test",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingRemark_throwsParseException() {
        assertParseFailure(parser, "2", String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_emptyRemark_returnsRemarkCommand() {
        assertParseSuccess(parser, "2 r/", new RemarkCommand(INDEX_SECOND_PERSON, new Remark("")));
    }
}
