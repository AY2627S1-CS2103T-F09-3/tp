package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.UntagCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new UntagCommand object.
 */
public class UntagCommandParser implements Parser<UntagCommand> {

    public static final String MESSAGE_INVALID_FORMAT =
            "Format error. Input format: untag INDEX t/TAG. Example: untag 1 t/friends";
    public static final String MESSAGE_INVALID_INDEX =
            "Invalid index! Please enter a positive integer index between 1 and the size of the current list.";
    public static final String MESSAGE_INVALID_TAG = "Tags should be alphanumeric with no spaces.";

    /**
     * Parses the given {@code String} of arguments in the context of the UntagCommand
     * and returns an UntagCommand object for execution.
     *
     * @throws ParseException If the user input does not conform to the expected format.
     */
    public UntagCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TAG);

        Tag tag = parseTag(argMultimap);
        Index index = parseIndex(argMultimap);
        return new UntagCommand(index, tag);
    }

    /**
     * Parses the displayed person index, reporting the index requirements for invalid input.
     *
     * @throws ParseException If the index is missing or is not a positive integer within the supported integer range.
     */
    private Index parseIndex(ArgumentMultimap argMultimap) throws ParseException {
        try {
            return ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(MESSAGE_INVALID_INDEX, pe);
        }
    }

    /**
     * Parses the tag to remove, requiring exactly one tag prefix and a valid tag value.
     *
     * @throws ParseException If the tag prefix is missing or repeated, or the tag value is invalid.
     */
    private Tag parseTag(ArgumentMultimap argMultimap) throws ParseException {
        if (argMultimap.getValue(PREFIX_TAG).isEmpty()) {
            throw new ParseException(MESSAGE_INVALID_FORMAT);
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_TAG);
        try {
            return ParserUtil.parseTag(argMultimap.getValue(PREFIX_TAG).get());
        } catch (ParseException pe) {
            throw new ParseException(MESSAGE_INVALID_TAG, pe);
        }
    }
}
