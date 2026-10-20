package seedu.address.model.search;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;

/**
 * Tests whether one customer field contains the complete search text, ignoring case.
 * Search text is a fragment, so it need not be a valid complete name, phone number, email or address.
 */
public final class TextContainsCriterion implements PersonCriterion {
    /**
     * Identifies the customer fields that support substring searches.
     */
    public enum Field {
        NAME, PHONE, EMAIL, ADDRESS
    }

    private final Field field;
    private final String query;

    /**
     * Creates a criterion for {@code field}, stripping surrounding whitespace from {@code query}.
     * Internal whitespace and punctuation remain significant. Case normalization is locale-independent.
     *
     * @throws NullPointerException If either argument is null.
     * @throws IllegalArgumentException If the query is blank.
     */
    public TextContainsCriterion(Field field, String query) {
        this.field = requireNonNull(field);
        String strippedQuery = requireNonNull(query).strip();
        checkArgument(!strippedQuery.isEmpty(), "Search text must not be blank.");
        this.query = strippedQuery.toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return getFieldValue(person).toLowerCase(Locale.ROOT).contains(query);
    }

    private String getFieldValue(Person person) {
        return switch (field) {
            case NAME -> person.getName().fullName;
            case PHONE -> person.getPhone().value;
            case EMAIL -> person.getEmail().value;
            case ADDRESS -> person.getAddress().value;
        };
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof TextContainsCriterion otherCriterion)) {
            return false;
        }

        return field == otherCriterion.field && query.equals(otherCriterion.query);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, query);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("field", field).add("query", query).toString();
    }
}
