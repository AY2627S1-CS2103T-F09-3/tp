package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNSUPPORTED_PREFIX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DETAILS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULFILMENT_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_FULFILMENT_METHOD;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HANDOVER_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_POSTAL_CODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.search.FulfilmentDateCriterion;
import seedu.address.model.search.FulfilmentMethodCriterion;
import seedu.address.model.search.ItemCriterion;
import seedu.address.model.search.PersonCriterion;
import seedu.address.model.search.PostalCodeCriterion;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TagCriterion;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.model.search.TextContainsCriterion.Field;

/**
 * Parses prefixed customer search criteria and creates a {@code FindCommand}.
 */
public class FindCommandParser implements Parser<FindCommand> {
    public static final String MESSAGE_NAME_QUERY_CONSTRAINTS =
            "Invalid name query! Use English letters, spaces, apostrophes or hyphens.";
    public static final String MESSAGE_PHONE_QUERY_CONSTRAINTS =
            "Invalid phone query! Use 1 to 8 digits; a complete phone number is not required.";
    public static final String MESSAGE_EMAIL_QUERY_CONSTRAINTS =
            "Invalid email query! Use letters, digits or +_.-@ without spaces; "
                    + "a complete email address is not required.";

    private static final List<Prefix> SUPPORTED_PREFIXES =
            List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_POSTAL_CODE, PREFIX_TAG,
                    PREFIX_DETAILS, PREFIX_STATUS, PREFIX_FULFILMENT_DATE, PREFIX_FULFILMENT_METHOD,
                    PREFIX_HANDOVER_NOTES);

    /**
     * Parses one or more prefixed criteria, combined using AND.
     *
     * @throws ParseException If prefixes are missing, unsupported or repeated, or any supplied value is invalid.
     */
    @Override
    public FindCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        // Supply a separator for the first prefix; only supported prefixes delimit values.
        String preparedArgs = " " + args.strip();
        Prefix[] prefixes = SUPPORTED_PREFIXES.toArray(Prefix[]::new);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(preparedArgs, prefixes);

        validateArguments(arguments, prefixes, formatError);
        List<PersonCriterion> criteria = createCustomerCriteria(arguments);
        addItemCriterion(criteria, arguments);
        if (criteria.isEmpty()) {
            throw new ParseException(formatError);
        }
        return new FindCommand(new SearchCriteria(criteria));
    }

    private void validateArguments(ArgumentMultimap arguments, Prefix[] prefixes, String formatError)
            throws ParseException {
        if (!arguments.getPreamble().isEmpty()) {
            throw new ParseException(formatError);
        }

        arguments.verifyNoDuplicatePrefixesFor(prefixes);
        for (Prefix prefix : SUPPORTED_PREFIXES) {
            Optional<String> value = arguments.getValue(prefix);
            if (value.isPresent() && value.get().isBlank()) {
                throw new ParseException(formatError);
            }
        }
    }

    private List<PersonCriterion> createCustomerCriteria(ArgumentMultimap arguments) throws ParseException {
        List<PersonCriterion> criteria = new ArrayList<>();
        addConstrainedTextCriterion(criteria, arguments, PREFIX_NAME, Field.NAME);
        addConstrainedTextCriterion(criteria, arguments, PREFIX_PHONE, Field.PHONE);
        addConstrainedTextCriterion(criteria, arguments, PREFIX_EMAIL, Field.EMAIL);
        addTextCriterion(criteria, arguments, PREFIX_ADDRESS, Field.ADDRESS);
        addTextCriterion(criteria, arguments, PREFIX_HANDOVER_NOTES, Field.HANDOVER_NOTES);
        if (arguments.getValue(PREFIX_FULFILMENT_DATE).isPresent()) {
            criteria.add(new FulfilmentDateCriterion(
                    ParserUtil.parseFulfilmentDate(arguments.getValue(PREFIX_FULFILMENT_DATE).get())));
        }
        if (arguments.getValue(PREFIX_FULFILMENT_METHOD).isPresent()) {
            criteria.add(new FulfilmentMethodCriterion(
                    ParserUtil.parseFulfilmentMethod(arguments.getValue(PREFIX_FULFILMENT_METHOD).get())));
        }
        if (arguments.getValue(PREFIX_POSTAL_CODE).isPresent()) {
            String postalCodePrefix = arguments.getValue(PREFIX_POSTAL_CODE).get().strip();
            if (!PostalCodeCriterion.isValidPrefix(postalCodePrefix)) {
                throw new ParseException(PostalCodeCriterion.MESSAGE_CONSTRAINTS);
            }
            criteria.add(new PostalCodeCriterion(postalCodePrefix));
        }
        if (arguments.getValue(PREFIX_TAG).isPresent()) {
            criteria.add(new TagCriterion(ParserUtil.parseTag(arguments.getValue(PREFIX_TAG).get().strip())));
        }

        return criteria;
    }

    private void addConstrainedTextCriterion(List<PersonCriterion> criteria, ArgumentMultimap arguments,
            Prefix prefix, Field field) throws ParseException {
        Optional<String> value = arguments.getValue(prefix);
        if (value.isEmpty()) {
            return;
        }

        String query = value.get();
        try {
            switch (field) {
                case NAME -> validateQuery(query, "[A-Za-z '-]+", MESSAGE_NAME_QUERY_CONSTRAINTS);
                case PHONE -> validateQuery(query, "[0-9]{1,8}", MESSAGE_PHONE_QUERY_CONSTRAINTS);
                case EMAIL -> validateQuery(query, "[A-Za-z0-9+_.@-]+", MESSAGE_EMAIL_QUERY_CONSTRAINTS);
                default -> throw new IllegalArgumentException("Unsupported constrained text field: " + field);
            }
        } catch (ParseException pe) {
            throw createFieldParseException(query, pe);
        }
        criteria.add(new TextContainsCriterion(field, query));
    }

    private void validateQuery(String query, String validationRegex, String constraintMessage) throws ParseException {
        if (!query.strip().matches(validationRegex)) {
            throw new ParseException(constraintMessage);
        }
    }

    private ParseException createFieldParseException(String value, ParseException cause) {
        String message = ParserUtil.findUnsupportedPrefix(value, SUPPORTED_PREFIXES.toArray(Prefix[]::new))
                .map(prefix -> String.format(MESSAGE_UNSUPPORTED_PREFIX, prefix, FindCommand.COMMAND_WORD))
                .orElse(cause.getMessage());
        return new ParseException(message, cause);
    }

    private void addItemCriterion(List<PersonCriterion> criteria, ArgumentMultimap arguments) throws ParseException {
        Optional<String> productName = arguments.getValue(PREFIX_DETAILS);
        Optional<String> status = arguments.getValue(PREFIX_STATUS);
        if (productName.isEmpty() && status.isEmpty()) {
            return;
        }

        // Omitting s/ accepts all statuses. An empty set would instead match no items.
        Set<ItemStatus> statuses = status.isPresent()
                ? Set.of(ParserUtil.parseItemStatus(status.get().strip()))
                : EnumSet.allOf(ItemStatus.class);
        // Both conditions belong in one criterion so they cannot match different items.
        criteria.add(new ItemCriterion(productName, statuses));
    }

    private void addTextCriterion(List<PersonCriterion> criteria, ArgumentMultimap arguments,
            Prefix prefix, Field field) {
        arguments.getValue(prefix).ifPresent(value -> criteria.add(new TextContainsCriterion(field, value)));
    }
}
