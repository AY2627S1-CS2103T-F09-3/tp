package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNSUPPORTED_PREFIX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.TagCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new TagCommand object
 */
public class TagCommandParser implements Parser<TagCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the TagCommand
     * and returns a TagCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public TagCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TAG);

        String formatErrorMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE);
        Index index = ParserUtil.parsePreambleIndex(argMultimap, formatErrorMessage);
        String value = argMultimap.getValue(PREFIX_TAG)
                .orElseThrow(() -> new ParseException(formatErrorMessage));
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_TAG);

        try {
            Tag tag = ParserUtil.parseTag(value);
            return new TagCommand(index, tag);
        } catch (ParseException pe) {
            String message = ParserUtil.findUnsupportedPrefix(value, PREFIX_TAG)
                    .map(prefix -> String.format(MESSAGE_UNSUPPORTED_PREFIX, prefix, TagCommand.COMMAND_WORD))
                    .orElse(pe.getMessage());
            throw new ParseException(message, pe);
        }
    }

}
