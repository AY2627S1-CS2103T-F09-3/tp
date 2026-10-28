package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_ITEM_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNSUPPORTED_PREFIX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ITEM_INDEX;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteItemCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteItemCommand object
 */
public class DeleteItemCommandParser implements Parser<DeleteItemCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteItemCommand
     * and returns a DeleteItemCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public DeleteItemCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argumentMultimap = ArgumentTokenizer.tokenize(args, PREFIX_ITEM_INDEX);
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteItemCommand.MESSAGE_USAGE);
        Index index = ParserUtil.parsePreambleIndex(argumentMultimap, usage);
        argumentMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ITEM_INDEX);
        Index itemIndex = parseItemIndex(argumentMultimap, usage);
        return new DeleteItemCommand(index, itemIndex);
    }

    private Index parseItemIndex(ArgumentMultimap arguments, String usage) throws ParseException {
        String value = arguments.getValue(PREFIX_ITEM_INDEX).orElseThrow(() -> new ParseException(usage));
        try {
            return ParserUtil.parseIndex(value);
        } catch (ParseException pe) {
            String message = ParserUtil.findUnsupportedPrefix(value, PREFIX_ITEM_INDEX)
                    .map(prefix -> String.format(MESSAGE_UNSUPPORTED_PREFIX, prefix, DeleteItemCommand.COMMAND_WORD))
                    .orElse(MESSAGE_INVALID_ITEM_INDEX);
            throw new ParseException(message, pe);
        }
    }
}
