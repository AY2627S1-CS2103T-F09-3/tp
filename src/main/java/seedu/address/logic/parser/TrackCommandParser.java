package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.Optional;

import seedu.address.logic.commands.TrackCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.preorder.PreorderStatus;

/**
 * Parses track command arguments and creates a new {@code TrackCommand}.
 */
public class TrackCommandParser implements Parser<TrackCommand> {

    /**
     * Parses the optional status into a track command.
     * Returns a command for active preorders when no arguments are supplied.
     *
     * @throws ParseException If the input does not conform to the track command format.
     */
    @Override
    public TrackCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(" " + args.trim(), PREFIX_STATUS);

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, TrackCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_STATUS);
        if (argMultimap.getValue(PREFIX_STATUS).isEmpty()) {
            return new TrackCommand(Optional.empty());
        }

        PreorderStatus status = ParserUtil.parsePreorderStatus(argMultimap.getValue(PREFIX_STATUS).get());
        return new TrackCommand(Optional.of(status));
    }
}
