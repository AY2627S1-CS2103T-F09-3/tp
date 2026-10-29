package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.GroupPostalCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the group postal command. */
public class GroupPostalParser implements Parser<GroupPostalCommand> {
    @Override
    public GroupPostalCommand parse(String args) throws ParseException {
        if (!GroupPostalCommand.POSTAL_ARGUMENT.equalsIgnoreCase(args.trim())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    GroupPostalCommand.MESSAGE_USAGE));
        }
        return new GroupPostalCommand();
    }
}
