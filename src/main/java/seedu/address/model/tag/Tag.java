package seedu.address.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Tag in the address book.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}.
 * Tags are compared case-insensitively, so {@code VIP} and {@code vip} are the same tag.
 */
public class Tag {

    /** Maximum length in UTF-16 code units. */
    public static final int MAX_LENGTH = 30;

    public static final String MESSAGE_CONSTRAINTS = "Tag names must not be blank and must contain only "
            + "letters (A-Z, a-z) and digits (0-9), with no spaces or special characters. "
            + "Tag names must be at most " + MAX_LENGTH + " characters long. Examples: VIP, friends, regular123.";
    public static final String VALIDATION_REGEX = "\\p{Alnum}+";

    public final String tagName;

    /**
     * Constructs a {@code Tag}.
     *
     * @param tagName A valid tag name.
     * @throws NullPointerException If {@code tagName} is null.
     * @throws IllegalArgumentException If {@code tagName} does not satisfy the tag constraints.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        checkArgument(isValidTagName(tagName), MESSAGE_CONSTRAINTS);
        this.tagName = tagName;
    }

    /**
     * Returns true if the string contains one or more ASCII letters or digits only.
     * The tag name must have at most {@link #MAX_LENGTH} UTF-16 code units.
     * Whitespace is not trimmed before validation.
     *
     * @throws NullPointerException If {@code test} is null.
     */
    public static boolean isValidTagName(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Tag otherTag)) {
            return false;
        }

        return tagName.equalsIgnoreCase(otherTag.tagName);
    }

    @Override
    public int hashCode() {
        return tagName.toLowerCase(Locale.ROOT).hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
