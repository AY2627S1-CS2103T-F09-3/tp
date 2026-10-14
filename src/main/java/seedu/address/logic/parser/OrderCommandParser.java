package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORDER_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ORDER_STATUS;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.OrderCommand;
import seedu.address.logic.commands.OrderCommand.EditPreorderDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses a displayed person index and optional preorder fields into an {@link OrderCommand}.
 * Whether both fields are required is checked during execution, using the person's existing preorder.
 */
public class OrderCommandParser implements Parser<OrderCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the OrderCommand
     * and returns an OrderCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public OrderCommand parse(String userInput) throws ParseException {
        requireNonNull(userInput);

        ArgumentMultimap argumentMultimap =
                ArgumentTokenizer.tokenize(userInput, PREFIX_ORDER_DETAILS, PREFIX_ORDER_STATUS);

        Index index;

        try {
            index = ParserUtil.parseIndex(argumentMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, OrderCommand.MESSAGE_USAGE), pe);
        }

        argumentMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ORDER_DETAILS, PREFIX_ORDER_STATUS);

        EditPreorderDescriptor descriptor = new EditPreorderDescriptor();
        if (argumentMultimap.getValue(PREFIX_ORDER_DETAILS).isPresent()) {
            descriptor.setDetails(
                    ParserUtil.parsePreorderDetails(argumentMultimap.getValue(PREFIX_ORDER_DETAILS).get()));
        }
        if (argumentMultimap.getValue(PREFIX_ORDER_STATUS).isPresent()) {
            descriptor.setStatus(ParserUtil.parsePreorderStatus(argumentMultimap.getValue(PREFIX_ORDER_STATUS).get()));
        }

        return new OrderCommand(index, descriptor);
    }

}
