package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Remark;

/** Parses arguments for {@link RemarkCommand}. */
public class RemarkCommandParser implements Parser<RemarkCommand> {
    @Override
    public RemarkCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_REMARK);
        try {
            Index index = ParserUtil.parseIndex(argMultimap.getPreamble());
            if (!argMultimap.getValue(PREFIX_REMARK).isPresent()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE));
            }
            String remark = argMultimap.getValue(PREFIX_REMARK).get();
            return new RemarkCommand(index, new Remark(remark));
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE), pe);
        }
    }
}
