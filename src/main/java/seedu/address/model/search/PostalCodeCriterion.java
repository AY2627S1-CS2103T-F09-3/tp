package seedu.address.model.search;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.person.PostalCode;

/**
 * Tests whether a customer has the exact requested postal code.
 * Customers without a postal code do not match.
 */
public final class PostalCodeCriterion implements PersonCriterion {
    private final PostalCode postalCode;

    /**
     * Creates a criterion for an already validated {@code postalCode}.
     *
     * @throws NullPointerException If {@code postalCode} is null.
     */
    public PostalCodeCriterion(PostalCode postalCode) {
        this.postalCode = requireNonNull(postalCode);
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return postalCode.equals(person.getPostalCode());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof PostalCodeCriterion otherCriterion)) {
            return false;
        }

        return postalCode.equals(otherCriterion.postalCode);
    }

    @Override
    public int hashCode() {
        return postalCode.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("postalCode", postalCode).toString();
    }
}
