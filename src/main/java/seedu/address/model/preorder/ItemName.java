package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents the name of an item in a customer's preorder, such as "Pineapple Tarts".
 * Guarantees: immutable; is valid as declared in {@link #isValidItemName(String)}
 */
public class ItemName {

    // Same limit as the order details in v1.3, so older preorders still load as items
    public static final int MAX_LENGTH = 200;

    public static final String MESSAGE_CONSTRAINTS = "Invalid item name! Item names must not be blank, "
            + "must be on a single line, and must be at most " + MAX_LENGTH + " characters long.";

    /*
     * The first and last characters must not be whitespace, so blank names and names with
     * untrimmed spaces are rejected. Line breaks are not allowed anywhere.
     */
    public static final String VALIDATION_REGEX = "[^\\s]([^\\r\\n]*[^\\s])?";

    public final String value;

    /**
     * Constructs an {@code ItemName}.
     *
     * @param name A valid item name.
     */
    public ItemName(String name) {
        requireNonNull(name);
        checkArgument(isValidItemName(name), MESSAGE_CONSTRAINTS);
        value = name;
    }

    /**
     * Returns true if a given string is a valid item name.
     */
    public static boolean isValidItemName(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
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
        if (!(other instanceof ItemName otherItemName)) {
            return false;
        }

        return value.equals(otherItemName.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
