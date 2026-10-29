package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_ITEM_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ITEM_INDEX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_QUANTITY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditItemCommand;
import seedu.address.logic.commands.EditItemCommand.EditItemDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses customer and item indices and optional item fields into an {@link EditItemCommand}.
 */
public class EditItemCommandParser implements Parser<EditItemCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the EditItemCommand
     * and returns an EditItemCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public EditItemCommand parse(String userInput) throws ParseException {
        requireNonNull(userInput);

        ArgumentMultimap argumentMultimap = ArgumentTokenizer.tokenize(userInput,
                PREFIX_ITEM_INDEX, PREFIX_DETAILS, PREFIX_QUANTITY, PREFIX_STATUS, PREFIX_REMARK);
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditItemCommand.MESSAGE_USAGE);
        Index index = ParserUtil.parsePreambleIndex(argumentMultimap, usage);
        argumentMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_ITEM_INDEX, PREFIX_DETAILS, PREFIX_QUANTITY, PREFIX_STATUS, PREFIX_REMARK);
        Index itemIndex = parseItemIndex(argumentMultimap, usage);
        EditItemDescriptor descriptor = parseDescriptor(argumentMultimap);
        if (!descriptor.isAnyFieldEdited()) {
            throw new ParseException(EditItemCommand.MESSAGE_NOT_EDITED);
        }
        return new EditItemCommand(index, itemIndex, descriptor);
    }

    private Index parseItemIndex(ArgumentMultimap arguments, String usage) throws ParseException {
        String value = arguments.getValue(PREFIX_ITEM_INDEX).orElseThrow(() -> new ParseException(usage));
        try {
            return ParserUtil.parseIndex(value);
        } catch (ParseException pe) {
            throw createFieldParseException(value, MESSAGE_INVALID_ITEM_INDEX, pe);
        }
    }

    private ParseException createFieldParseException(String value, String fallbackMessage, ParseException cause) {
        return ParserUtil.createFieldParseException(value, cause, EditItemCommand.COMMAND_WORD, fallbackMessage,
                PREFIX_ITEM_INDEX, PREFIX_DETAILS, PREFIX_QUANTITY, PREFIX_STATUS, PREFIX_REMARK);
    }

    private EditItemDescriptor parseDescriptor(ArgumentMultimap argumentMultimap) throws ParseException {
        EditItemDescriptor descriptor = new EditItemDescriptor();
        if (argumentMultimap.getValue(PREFIX_DETAILS).isPresent()) {
            descriptor.setName(
                    ParserUtil.parseItemName(argumentMultimap.getValue(PREFIX_DETAILS).get()));
        }
        if (argumentMultimap.getValue(PREFIX_QUANTITY).isPresent()) {
            String quantity = argumentMultimap.getValue(PREFIX_QUANTITY).get();
            try {
                descriptor.setQuantity(ParserUtil.parseQuantity(quantity));
            } catch (ParseException pe) {
                throw createFieldParseException(quantity, pe.getMessage(), pe);
            }
        }
        if (argumentMultimap.getValue(PREFIX_STATUS).isPresent()) {
            String status = argumentMultimap.getValue(PREFIX_STATUS).get();
            try {
                descriptor.setStatus(ParserUtil.parseItemStatus(status));
            } catch (ParseException pe) {
                throw createFieldParseException(status, pe.getMessage(), pe);
            }
        }
        if (argumentMultimap.getValue(PREFIX_REMARK).isPresent()) {
            String remark = argumentMultimap.getValue(PREFIX_REMARK).get();
            descriptor.setRemark(remark.isEmpty() ? Optional.empty() : Optional.of(ParserUtil.parseRemark(remark)));
        }
        return descriptor;
    }

}
