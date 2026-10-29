package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.util.StringUtil;

/**
 * Represents the name of an item in a customer's preorder, such as "Pineapple Tarts".
 * Guarantees: immutable; new values are valid as declared in {@link #isValidItemName(String)}; legacy values loaded
 * from storage may contain forward slashes;
 * has no leading, trailing or repeated spaces, so a typing slip cannot split one product in two.
 */
public class ItemName {

    // The same limit as the order details in v1.3
    public static final int MAX_LENGTH = 200;

    public static final String MESSAGE_CONSTRAINTS = "Invalid item name! Item names must not be blank, "
            + "must not contain forward slashes, must be on a single line, and must be at most " + MAX_LENGTH
            + " characters long.";

    /*
     * The first and last characters must not be whitespace, so blank names and names with
     * untrimmed spaces are rejected. Unicode spaces, such as an em space, count as whitespace, and
     * at least one character must be visible, so a name of zero-width spaces is blank too.
     * Line breaks are not allowed anywhere.
     */
    public static final String VALIDATION_REGEX = "(?U)(?=.*[^\\p{Z}\\p{C}])[^\\s]([^\\v]*[^\\s])?";

    public final String value;

    /**
     * Constructs an {@code ItemName}, removing surrounding spaces and replacing repeated spaces with one.
     *
     * @param name A valid item name.
     */
    public ItemName(String name) {
        requireNonNull(name);
        checkArgument(isValidItemName(name), MESSAGE_CONSTRAINTS);
        value = StringUtil.normalizeSpaces(name);
    }

    private ItemName(String name, boolean isLegacyData) {
        checkArgument(isLegacyData, "Legacy item names must be created through the legacy-data factory.");
        value = name;
    }

    /**
     * Creates an item name loaded from legacy data, where forward slashes were allowed.
     * New user input must use {@link #ItemName(String)} so the current validation rule is enforced.
     *
     * @param name A legacy item name that satisfies all constraints except the slash restriction.
     */
    public static ItemName fromLegacyData(String name) {
        requireNonNull(name);
        String normalizedName = StringUtil.normalizeSpaces(name);
        checkArgument(isValidNameExceptSlash(normalizedName), MESSAGE_CONSTRAINTS);
        checkArgument(normalizedName.contains("/"), "Legacy item names must contain a forward slash.");
        return new ItemName(normalizedName, true);
    }

    /**
     * Returns true if a given string is a valid item name once surrounding spaces are removed
     * and repeated spaces are replaced with one.
     */
    public static boolean isValidItemName(String test) {
        String normalizedName = StringUtil.normalizeSpaces(test);
        return isValidNameExceptSlash(normalizedName) && !normalizedName.contains("/");
    }

    private static boolean isValidNameExceptSlash(String name) {
        return name.length() <= MAX_LENGTH && name.matches(VALIDATION_REGEX);
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
