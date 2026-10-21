package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_POSTAL_CODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
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
    private static final List<Prefix> SUPPORTED_PREFIXES =
            List.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_POSTAL_CODE, PREFIX_TAG);
    private static final Pattern PARAMETER_PREFIX = Pattern.compile("(?<!\\S)([A-Za-z]+/)");

    /**
     * Parses one or more prefixed criteria, combined using AND.
     *
     * @throws ParseException If prefixes are missing, unsupported or repeated, or any supplied value is invalid.
     */
    @Override
    public FindCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
        String preparedArgs = prepareArguments(args.strip(), formatError);
        Prefix[] prefixes = SUPPORTED_PREFIXES.toArray(Prefix[]::new);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(preparedArgs, prefixes);

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

        List<PersonCriterion> criteria = new ArrayList<>();
        addTextCriterion(criteria, arguments, PREFIX_NAME, Field.NAME);
        addTextCriterion(criteria, arguments, PREFIX_PHONE, Field.PHONE);
        addTextCriterion(criteria, arguments, PREFIX_EMAIL, Field.EMAIL);
        addTextCriterion(criteria, arguments, PREFIX_ADDRESS, Field.ADDRESS);
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

        if (criteria.isEmpty()) {
            throw new ParseException(formatError);
        }
        return new FindCommand(new SearchCriteria(criteria));
    }

    private String prepareArguments(String args, String formatError) throws ParseException {
        Matcher matcher = PARAMETER_PREFIX.matcher(args);
        while (matcher.find()) {
            if (!SUPPORTED_PREFIXES.contains(new Prefix(matcher.group(1)))) {
                throw new ParseException(formatError);
            }
        }

        // Let the shared tokenizer recognize the first prefix and prefixes after tabs or newlines.
        // Only add separator spaces before prefixes; preserve the text inside search values.
        return matcher.replaceAll(match -> match.start() > 0 && args.charAt(match.start() - 1) == ' '
                ? match.group(1) : " " + match.group(1));
    }

    private void addTextCriterion(List<PersonCriterion> criteria, ArgumentMultimap arguments,
            Prefix prefix, Field field) {
        arguments.getValue(prefix).ifPresent(value -> criteria.add(new TextContainsCriterion(field, value)));
    }
}
