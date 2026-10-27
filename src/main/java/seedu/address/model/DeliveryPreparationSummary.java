package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.search.ItemCriterion;

/** An immutable snapshot of outstanding delivery stops in the current customer results. */
public final class DeliveryPreparationSummary {
    private static final ItemCriterion OUTSTANDING_ITEMS =
            new ItemCriterion(Optional.empty(), Set.of(ItemStatus.PENDING, ItemStatus.PREPARED));

    private final List<Person> stops;

    /**
     * Selects DELIVERY customers with an outstanding item, counting each customer once.
     * Empty preorders, collection, unset methods and completed-only preorders do not contribute.
     * Customers without postal codes remain included, after customers with codes.
     */
    public DeliveryPreparationSummary(Collection<Person> customers) {
        stops = List.copyOf(requireNonNull(customers)).stream()
                .filter(OUTSTANDING_ITEMS)
                .filter(person -> person.getPreorder().flatMap(Preorder::getMethod)
                        .filter(FulfilmentMethod.DELIVERY::equals).isPresent())
                .sorted(Comparator.comparing((Person person) -> person.getPostalCode() == null
                        ? "\uffff" : person.getPostalCode().value)
                        .thenComparing(person -> person.getName().fullName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public List<Person> getStops() {
        return stops;
    }

    public int getStopCount() {
        return stops.size();
    }

    /** Returns the number of two-digit postal groups, including one group for missing postal codes. */
    public int getPostalGroupCount() {
        return (int) stops.stream().map(person -> person.getPostalCode() == null
                ? "" : person.getPostalCode().value.substring(0, 2)).distinct().count();
    }
}
