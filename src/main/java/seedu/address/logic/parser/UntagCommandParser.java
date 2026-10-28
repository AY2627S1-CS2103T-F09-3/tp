package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNSUPPORTED_PREFIX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.UntagCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new UntagCommand object.
 */
public class UntagCommandParser implements Parser<UntagCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the UntagCommand
     * and returns an UntagCommand object for execution.
     * Validates the index format before checking the tag prefix and value.
     *
     * @throws ParseException If the user input does not conform to the expected format.
     */
    public UntagCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TAG);

        String formatErrorMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE);
        Index index = ParserUtil.parsePreambleIndex(argMultimap, formatErrorMessage);
        String value = argMultimap.getValue(PREFIX_TAG)
                .orElseThrow(() -> new ParseException(formatErrorMessage));
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_TAG);

        try {
            Tag tag = ParserUtil.parseTag(value);
            return new UntagCommand(index, tag);
        } catch (ParseException pe) {
            String message = ParserUtil.findUnsupportedPrefix(value, PREFIX_TAG)
                    .map(prefix -> String.format(MESSAGE_UNSUPPORTED_PREFIX, prefix, UntagCommand.COMMAND_WORD))
                    .orElse(pe.getMessage());
            throw new ParseException(message, pe);
        }
    }
}
