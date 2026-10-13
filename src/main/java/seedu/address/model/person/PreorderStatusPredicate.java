package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.preorder.PreorderStatus;

/**
 * Tests that a {@code Person} has a preorder with one of the accepted statuses.
 * Persons without a preorder never match.
 */
public class PreorderStatusPredicate implements Predicate<Person> {
    private final Set<PreorderStatus> statuses;

    /**
     * Creates a predicate with an immutable copy of the accepted {@code statuses}.
     * An empty set matches no persons.
     *
     * @throws NullPointerException If {@code statuses} or any of its elements is null.
     */
    public PreorderStatusPredicate(Set<PreorderStatus> statuses) {
        requireNonNull(statuses);
        this.statuses = Set.copyOf(statuses);
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return person.getPreorder()
                .map(preorder -> statuses.contains(preorder.getStatus()))
                .orElse(false);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof PreorderStatusPredicate otherPreorderStatusPredicate)) {
            return false;
        }

        return statuses.equals(otherPreorderStatusPredicate.statuses);
    }

    @Override
    public int hashCode() {
        return statuses.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("statuses", statuses).toString();
    }
}
