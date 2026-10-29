package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.regex.Pattern;

/**
 * Represents a Person's address in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidAddress(String)}
 */
public class Address {

    /** Maximum length in UTF-16 code units. */
    public static final int MAX_LENGTH = 300;

    public static final String MESSAGE_CONSTRAINTS = "Addresses must not be blank or contain a letter-based prefix "
            + "(letters followed by '/') at the start or after whitespace, except c/o.";

    public static final String MESSAGE_LENGTH_CONSTRAINTS = "Addresses must be at most " + MAX_LENGTH
            + " characters long.";

    /*
     * The first character of the address must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String VALIDATION_REGEX = "[^\\s].*";

    private static final Pattern PREFIX_LIKE_SLASH_PATTERN = Pattern.compile("(?:^|\\s)(?!c/o(?:\\s|$))[A-Za-z]+/");

    public final String value;

    /**
     * Constructs an {@code Address}.
     *
     * @param address A valid address.
     */
    public Address(String address) {
        requireNonNull(address);
        checkArgument(isValidAddress(address), getValidationErrorMessage(address));
        value = address;
    }

    /**
     * Returns true if a given string is at most {@link #MAX_LENGTH} UTF-16 code units and has no letter-based prefix
     * (letters followed by '/') at the start or after whitespace, except conventional {@code c/o}.
     */
    public static boolean isValidAddress(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX)
                && !PREFIX_LIKE_SLASH_PATTERN.matcher(test).find();
    }

    /**
     * Returns the length constraint message if the address exceeds the limit,
     * or the format constraint message otherwise. Length violations take precedence over format violations.
     */
    public static String getValidationErrorMessage(String test) {
        return test.length() > MAX_LENGTH ? MESSAGE_LENGTH_CONSTRAINTS : MESSAGE_CONSTRAINTS;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Address otherAddress)) {
            return false;
        }

        return value.equals(otherAddress.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
