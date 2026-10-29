package seedu.address.model.search;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;

/**
 * Tests whether a customer's postal code starts with the requested digits.
 */
public final class PostalCodeCriterion implements PersonCriterion {
    public static final String MESSAGE_CONSTRAINTS = "Postal-code search values must contain one to six digits.";

    private final String postalCodePrefix;

    /**
     * Creates a criterion for a one-to-six-digit {@code postalCodePrefix}.
     *
     * @throws NullPointerException If {@code postalCodePrefix} is null.
     * @throws IllegalArgumentException If {@code postalCodePrefix} is not one to six digits.
     */
    public PostalCodeCriterion(String postalCodePrefix) {
        requireNonNull(postalCodePrefix);
        checkArgument(isValidPrefix(postalCodePrefix), MESSAGE_CONSTRAINTS);
        this.postalCodePrefix = postalCodePrefix;
    }

    /**
     * Returns the search digits, preserving leading zeros and the supplied prefix length.
     */
    public String getPostalCodePrefix() {
        return postalCodePrefix;
    }

    /**
     * Returns whether {@code prefix} contains one to six ASCII digits.
     */
    public static boolean isValidPrefix(String prefix) {
        return prefix.matches("[0-9]{1,6}");
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return person.getPostalCode().value.startsWith(postalCodePrefix);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof PostalCodeCriterion otherCriterion)) {
            return false;
        }

        return postalCodePrefix.equals(otherCriterion.postalCodePrefix);
    }

    @Override
    public int hashCode() {
        return postalCodePrefix.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("postalCodePrefix", postalCodePrefix).toString();
    }
}
