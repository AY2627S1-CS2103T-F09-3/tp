package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORDER_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORDER_STATUS;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.OrderCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class OrderCommandParser implements Parser<OrderCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the OrderCommand
     * and returns an OrderCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public OrderCommand parse(String userInput) throws ParseException {
        requireNonNull(userInput);

        ArgumentMultimap argumentMultimap = ArgumentTokenizer.tokenize(userInput, PREFIX_ORDER_DETAILS, PREFIX_ORDER_STATUS);

        Index index;

        try {
            index = ParserUtil.parseIndex(argumentMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, OrderCommand.MESSAGE_USAGE), pe);
        }

        argumentMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ORDER_DETAILS, PREFIX_ORDER_STATUS);

        String preorderDetailInput = argumentMultimap.getValue(PREFIX_ORDER_DETAILS).orElse("");
        String preorderStatusInput = argumentMultimap.getValue(PREFIX_ORDER_STATUS).orElse("");

        return new OrderCommand(index, preorderDetailInput, preorderStatusInput);
    }

}
