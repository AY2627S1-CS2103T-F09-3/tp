package seedu.address.model.search;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;

/**
 * Tests whether a customer satisfies every criterion in an immutable search.
 * An empty search matches every customer. Criterion order and repetition do not affect equality or matching.
 */
public final class SearchCriteria implements Predicate<Person> {
    private final Set<PersonCriterion> criteria;

    /**
     * Creates a search using a defensive copy of {@code criteria}.
     *
     * @throws NullPointerException If {@code criteria} or any of its elements is null.
     */
    public SearchCriteria(Collection<? extends PersonCriterion> criteria) {
        requireNonNull(criteria);
        this.criteria = Set.copyOf(criteria);
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return criteria.stream().allMatch(criterion -> criterion.test(person));
    }

    /**
     * Returns whether this search contains no criteria.
     */
    public boolean isEmpty() {
        return criteria.isEmpty();
    }

    /**
     * Returns the stored criterion count and sorted, distinct criterion type names for diagnostic logging.
     */
    public String getLogSummary() {
        List<String> types = criteria.stream()
                .map(criterion -> criterion.getClass().getSimpleName())
                .distinct()
                .sorted()
                .toList();
        return "count=" + criteria.size() + ", types=" + types;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof SearchCriteria otherCriteria)) {
            return false;
        }

        return criteria.equals(otherCriteria.criteria);
    }

    @Override
    public int hashCode() {
        return criteria.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("criteria", criteria).toString();
    }
}
