package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)};
 * has no leading, trailing or consecutive spaces; preserves capitalization.
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Invalid name! Names must contain at least one English letter and only English letters, spaces, "
                    + "apostrophes or hyphens.";

    public static final String VALIDATION_REGEX = "(?=.*[A-Za-z])[A-Za-z '-]+";

    public final String fullName;

    /**
     * Constructs a {@code Name}, trimming surrounding whitespace and collapsing consecutive spaces.
     * Capitalization is preserved.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name.trim().replaceAll(" +", " ");
    }

    /**
     * Returns true if a given string contains at least one English letter and only English letters,
     * spaces, apostrophes or hyphens after trimming surrounding whitespace.
     */
    public static boolean isValidName(String test) {
        String trimmedName = test.trim();
        return !trimmedName.isEmpty() && trimmedName.matches(VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
