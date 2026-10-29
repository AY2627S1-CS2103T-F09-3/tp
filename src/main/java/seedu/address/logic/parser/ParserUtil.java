package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_UNSUPPORTED_PREFIX;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.PostalCode;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.preorder.Remark;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    private static final Pattern PREFIX_LIKE_TOKEN = Pattern.compile("\\s([A-Za-z]+/)");

    /**
     * Returns the first letter-based prefix after whitespace in {@code value} that is not supported by the command.
     * Use this only to refine errors for an already-invalid constrained field, as free text can contain literal
     * prefix-like text.
     *
     * @param value An invalid field value to inspect.
     * @param supportedPrefixes Prefixes supported by the command.
     */
    public static Optional<Prefix> findUnsupportedPrefix(String value, Prefix... supportedPrefixes) {
        requireNonNull(value);
        requireNonNull(supportedPrefixes);
        Matcher matcher = PREFIX_LIKE_TOKEN.matcher(value);
        while (matcher.find()) {
            Prefix prefix = new Prefix(matcher.group(1));
            if (Arrays.stream(supportedPrefixes).noneMatch(prefix::equals)) {
                return Optional.of(prefix);
            }
        }
        return Optional.empty();
    }

    /**
     * Creates an error that names an unsupported prefix or keeps the original field constraint message.
     * Call this only after constrained-field validation fails. The original exception remains the direct cause.
     *
     * @param value The invalid field value to inspect.
     * @param cause The field validation failure.
     * @param commandWord The command name used in an unsupported-prefix message.
     * @param supportedPrefixes Prefixes supported by the command.
     */
    public static ParseException createFieldParseException(String value, ParseException cause, String commandWord,
            Prefix... supportedPrefixes) {
        requireNonNull(cause);
        return createFieldParseException(value, cause, commandWord, cause.getMessage(), supportedPrefixes);
    }

    /**
     * Creates an error that names an unsupported prefix or uses a supplied fallback message.
     * The original exception remains the direct cause. Use this overload for fields with a specialized message,
     * such as an item index parsed through the generic index validator.
     *
     * @param value The invalid field value to inspect.
     * @param cause The field validation failure.
     * @param commandWord The command name used in an unsupported-prefix message.
     * @param fallbackMessage The message used when no unsupported prefix is found.
     * @param supportedPrefixes Prefixes supported by the command.
     */
    public static ParseException createFieldParseException(String value, ParseException cause, String commandWord,
            String fallbackMessage, Prefix... supportedPrefixes) {
        requireNonNull(cause);
        requireNonNull(commandWord);
        String message = findUnsupportedPrefix(value, supportedPrefixes)
                .map(prefix -> String.format(MESSAGE_UNSUPPORTED_PREFIX, prefix, commandWord))
                .orElse(fallbackMessage);
        return new ParseException(message, cause);
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespace is trimmed, and consecutive spaces are collapsed.
     * Capitalization is preserved.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        if (!Name.isValidName(name)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(name);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String postalCode} into a {@code PostalCode}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code postalCode} is invalid.
     */
    public static PostalCode parsePostalCode(String postalCode) throws ParseException {
        requireNonNull(postalCode);
        String trimmedPostalCode = postalCode.trim();
        if (!PostalCode.isValidPostalCode(trimmedPostalCode)) {
            throw new ParseException(PostalCode.MESSAGE_CONSTRAINTS);
        }
        return new PostalCode(trimmedPostalCode);
    }

    /**
     * Parses a real fulfilment date in YYYY-MM-DD format, ignoring surrounding whitespace.
     *
     * @throws ParseException If the date is invalid.
     */
    public static FulfilmentDate parseFulfilmentDate(String date) throws ParseException {
        String strippedDate = requireNonNull(date).strip();
        if (!FulfilmentDate.isValidDate(strippedDate)) {
            throw new ParseException(FulfilmentDate.MESSAGE_CONSTRAINTS);
        }
        return new FulfilmentDate(strippedDate);
    }

    /**
     * Parses DELIVERY or COLLECTION, ignoring case and surrounding whitespace.
     *
     * @throws ParseException If the method is invalid.
     */
    public static FulfilmentMethod parseFulfilmentMethod(String method) throws ParseException {
        String normalizedMethod = requireNonNull(method).strip().toUpperCase(Locale.ROOT);
        if (!FulfilmentMethod.isValidMethod(normalizedMethod)) {
            throw new ParseException(FulfilmentMethod.MESSAGE_CONSTRAINTS);
        }
        return FulfilmentMethod.valueOf(normalizedMethod);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses the preamble of {@code argMultimap} into an {@code Index}.
     *
     * @param argMultimap Tokenized arguments containing the index in the preamble.
     * @param errorMessage Complete error message shown when the index is missing or invalid.
     * @throws ParseException If the preamble is not a positive integer within the supported integer range.
     */
    public static Index parsePreambleIndex(ArgumentMultimap argMultimap, String errorMessage)
            throws ParseException {
        requireNonNull(argMultimap);
        requireNonNull(errorMessage);
        try {
            return parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(errorMessage, pe);
        }
    }

    /**
     * Parses the single {@code t/TAG} value in {@code argMultimap} into a {@code Tag}.
     * Checks for missing and duplicate prefixes before validating the tag. Refines tag-value failures using the
     * command's unsupported-prefix diagnostic.
     *
     * @param argMultimap Tokenized arguments containing the tag prefix and value.
     * @param errorMessage Complete error message shown when the tag prefix is missing.
     * @param commandWord The command name used in an unsupported-prefix message.
     * @throws ParseException If the tag prefix is missing or repeated, or the tag is invalid.
     */
    public static Tag parseSingleTag(ArgumentMultimap argMultimap, String errorMessage, String commandWord)
            throws ParseException {
        requireNonNull(argMultimap);
        requireNonNull(errorMessage);
        requireNonNull(commandWord);
        String value = argMultimap.getValue(PREFIX_TAG).orElseThrow(() -> new ParseException(errorMessage));

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_TAG);
        try {
            return parseTag(value);
        } catch (ParseException pe) {
            throw createFieldParseException(value, pe, commandWord, PREFIX_TAG);
        }
    }

    /**
     * Parses a {@code String name} into an {@code ItemName}.
     * Leading and trailing whitespace is removed, and repeated spaces are replaced with one.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static ItemName parseItemName(String name) throws ParseException {
        requireNonNull(name);
        if (!ItemName.isValidItemName(name)) {
            throw new ParseException(ItemName.MESSAGE_CONSTRAINTS);
        }
        return new ItemName(name);
    }

    /**
     * Parses a {@code String quantity} into a {@code Quantity}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code quantity} is invalid.
     */
    public static Quantity parseQuantity(String quantity) throws ParseException {
        requireNonNull(quantity);
        String trimmedQuantity = quantity.trim();
        if (!Quantity.isValidQuantity(trimmedQuantity)) {
            throw new ParseException(Quantity.MESSAGE_CONSTRAINTS);
        }
        return new Quantity(trimmedQuantity);
    }

    /**
     * Parses a {@code String remark} into a {@code Remark}.
     * Leading and trailing whitespaces, including Unicode spaces, will be trimmed.
     *
     * @throws ParseException if the given {@code remark} is invalid.
     */
    public static Remark parseRemark(String remark) throws ParseException {
        requireNonNull(remark);
        String trimmedRemark = StringUtil.stripSpaces(remark);
        if (!Remark.isValidRemark(trimmedRemark)) {
            throw new ParseException(Remark.MESSAGE_CONSTRAINTS);
        }
        return new Remark(trimmedRemark);
    }

    /**
     * Parses a {@code String status} into a {@code ItemStatus}.
     * Leading and trailing whitespaces will be trimmed, and letter case is ignored,
     * so {@code "pending"} and {@code "Pending"} both give {@code PENDING}.
     *
     * @throws ParseException if the given {@code status} is invalid.
     */
    public static ItemStatus parseItemStatus(String status) throws ParseException {
        requireNonNull(status);
        String normalizedStatus = status.trim().toUpperCase(Locale.ROOT);
        if (!ItemStatus.isValidStatus(normalizedStatus)) {
            throw new ParseException(ItemStatus.MESSAGE_CONSTRAINTS);
        }
        return ItemStatus.valueOf(normalizedStatus);
    }

    /**
     * Parses a {@code String handoverNotes} into a {@code HandoverNotes}.
     * Leading and trailing whitespaces, including Unicode spaces, will be trimmed.
     *
     * @throws ParseException if the given {@code handoverNotes} is invalid.
     */
    public static HandoverNotes parseHandoverNotes(String handoverNotes) throws ParseException {
        requireNonNull(handoverNotes);
        String trimmedHandoverNotes = StringUtil.stripSpaces(handoverNotes);
        if (!HandoverNotes.isValidHandoverNotes(trimmedHandoverNotes)) {
            throw new ParseException(HandoverNotes.MESSAGE_CONSTRAINTS);
        }
        return new HandoverNotes(trimmedHandoverNotes);
    }
}
