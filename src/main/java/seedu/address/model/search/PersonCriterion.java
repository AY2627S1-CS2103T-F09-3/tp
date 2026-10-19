package seedu.address.model.search;

import java.util.function.Predicate;

import seedu.address.model.person.Person;

/**
 * Tests one condition of a customer search.
 * Implementations must be immutable and provide value-based equality and hash codes so that searches can be compared.
 * Conditions on the same preorder item must be combined within one criterion before it is added to a search.
 */
@FunctionalInterface
public interface PersonCriterion extends Predicate<Person> {
}
