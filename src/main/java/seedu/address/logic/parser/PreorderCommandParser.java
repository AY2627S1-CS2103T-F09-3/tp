package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULFILMENT_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULFILMENT_METHOD;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HANDOVER_NOTES;

import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.PreorderCommand;
import seedu.address.logic.commands.PreorderCommand.EditFulfilmentDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new PreorderCommand object
 */
public class PreorderCommandParser implements Parser<PreorderCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the PreorderCommand
     * and returns a PreorderCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public PreorderCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args,
                PREFIX_FULFILMENT_DATE, PREFIX_FULFILMENT_METHOD, PREFIX_HANDOVER_NOTES);

        String formatErrorMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, PreorderCommand.MESSAGE_USAGE);
        Index index = ParserUtil.parsePreambleIndex(argMultimap, formatErrorMessage);

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_FULFILMENT_DATE, PREFIX_FULFILMENT_METHOD,
                PREFIX_HANDOVER_NOTES);
        EditFulfilmentDescriptor editFulfilmentDescriptor = parseDescriptor(argMultimap);

        if (!editFulfilmentDescriptor.isAnyFieldChanged()) {
            throw new ParseException(PreorderCommand.MESSAGE_NOT_CHANGED);
        }

        return new PreorderCommand(index, editFulfilmentDescriptor);
    }

    /**
     * Returns the fulfilment details supplied in {@code argMultimap}. An empty value clears that detail.
     *
     * @throws ParseException if a supplied value is invalid.
     */
    private static EditFulfilmentDescriptor parseDescriptor(ArgumentMultimap argMultimap) throws ParseException {
        EditFulfilmentDescriptor descriptor = new EditFulfilmentDescriptor();

        Optional<String> date = argMultimap.getValue(PREFIX_FULFILMENT_DATE);
        if (date.isPresent()) {
            descriptor.setDate(date.get().isEmpty() ? null : ParserUtil.parseFulfilmentDate(date.get()));
        }

        Optional<String> method = argMultimap.getValue(PREFIX_FULFILMENT_METHOD);
        if (method.isPresent()) {
            descriptor.setMethod(method.get().isEmpty() ? null : ParserUtil.parseFulfilmentMethod(method.get()));
        }

        Optional<String> handoverNotes = argMultimap.getValue(PREFIX_HANDOVER_NOTES);
        if (handoverNotes.isPresent()) {
            descriptor.setHandoverNotes(handoverNotes.get().isEmpty()
                    ? null : ParserUtil.parseHandoverNotes(handoverNotes.get()));
        }

        return descriptor;
    }

}
