package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_QUANTITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.Optional;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddItemCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.preorder.Remark;

/**
 * Parses customer index and item fields into an {@link AddItemCommand}.
 */
public class AddItemCommandParser implements Parser<AddItemCommand> {

    /**
     * Parses a customer index, required item fields and an optional non-blank remark.
     *
     * @throws ParseException if the input has missing, repeated or invalid fields.
     */
    @Override
    public AddItemCommand parse(String userInput) throws ParseException {
        requireNonNull(userInput);

        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(userInput, PREFIX_DETAILS, PREFIX_QUANTITY, PREFIX_STATUS, PREFIX_REMARK);
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddItemCommand.MESSAGE_USAGE);

        Index index = ParserUtil.parsePreambleIndex(argMultimap, usage);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_DETAILS, PREFIX_QUANTITY, PREFIX_STATUS, PREFIX_REMARK);
        if (!arePrefixesPresent(argMultimap, PREFIX_DETAILS, PREFIX_QUANTITY, PREFIX_STATUS)) {
            throw new ParseException(usage);
        }

        Item item = parseItem(argMultimap);
        return new AddItemCommand(index, item);
    }

    /**
     * Constructs a validated item after checking that all required prefixes are present.
     */
    private Item parseItem(ArgumentMultimap argMultimap) throws ParseException {
        assert arePrefixesPresent(argMultimap, PREFIX_DETAILS, PREFIX_QUANTITY, PREFIX_STATUS)
                : "Required prefixes must be checked before parsing an item";
        ItemName itemName = ParserUtil.parseItemName(argMultimap.getValue(PREFIX_DETAILS).get());
        String quantityValue = argMultimap.getValue(PREFIX_QUANTITY).get();
        Quantity quantity;
        try {
            quantity = ParserUtil.parseQuantity(quantityValue);
        } catch (ParseException pe) {
            throw createFieldParseException(quantityValue, pe);
        }
        String statusValue = argMultimap.getValue(PREFIX_STATUS).get();
        ItemStatus status;
        try {
            status = ParserUtil.parseItemStatus(statusValue);
        } catch (ParseException pe) {
            throw createFieldParseException(statusValue, pe);
        }
        Optional<Remark> remark = parseRemark(argMultimap);
        return new Item(itemName, Optional.of(quantity), status, remark);
    }

    private ParseException createFieldParseException(String value, ParseException cause) {
        return ParserUtil.createFieldParseException(value, cause, AddItemCommand.COMMAND_WORD,
                PREFIX_DETAILS, PREFIX_QUANTITY, PREFIX_STATUS, PREFIX_REMARK);
    }

    /**
     * Returns no remark when omitted, but rejects an explicitly empty or invalid remark.
     */
    private Optional<Remark> parseRemark(ArgumentMultimap argMultimap) throws ParseException {
        Optional<String> remark = argMultimap.getValue(PREFIX_REMARK);
        if (remark.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ParserUtil.parseRemark(remark.get()));
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
