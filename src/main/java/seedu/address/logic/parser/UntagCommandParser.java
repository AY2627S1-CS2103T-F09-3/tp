package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
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
     *
     * @throws ParseException If the user input does not conform to the expected format.
     */
    public UntagCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TAG);

        Index index = parseIndex(argMultimap);
        Tag tag = parseTag(argMultimap);
        return new UntagCommand(index, tag);
    }

    /**
     * Parses the displayed person index, including command usage in errors for invalid input.
     *
     * @throws ParseException If the index is missing or is not a positive integer within the supported integer range.
     */
    private Index parseIndex(ArgumentMultimap argMultimap) throws ParseException {
        try {
            return ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE), pe);
        }
    }

    /**
     * Parses the tag to remove, requiring exactly one tag prefix and a valid tag value.
     *
     * @throws ParseException If the tag prefix is missing or repeated, or the tag value is invalid.
     */
    private Tag parseTag(ArgumentMultimap argMultimap) throws ParseException {
        if (argMultimap.getValue(PREFIX_TAG).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_TAG);
        return ParserUtil.parseTag(argMultimap.getValue(PREFIX_TAG).get());
    }
}
