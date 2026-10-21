package seedu.address.model.search;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;

/**
 * Matches customers with an item satisfying both the product and status conditions.
 * Immutable and reusable for customer filtering, item counts and item highlighting.
 */
public final class ItemCriterion implements PersonCriterion {
    private final Optional<String> productName;
    private final Set<ItemStatus> statuses;

    /**
     * Creates a criterion for an optional complete product name and accepted statuses.
     * An absent product matches any product; an empty status set matches no items.
     * Product names ignore case and surrounding whitespace, preserving internal spacing and punctuation.
     *
     * @throws NullPointerException If either argument or a status element is null.
     * @throws IllegalArgumentException If the supplied product name is blank.
     */
    public ItemCriterion(Optional<String> productName, Set<ItemStatus> statuses) {
        this.productName = requireNonNull(productName).map(ItemCriterion::normalizeProductName);
        this.statuses = Set.copyOf(requireNonNull(statuses));
    }

    private static String normalizeProductName(String productName) {
        String strippedName = productName.strip();
        checkArgument(!strippedName.isEmpty(), "Product search text must not be blank.");
        return strippedName.toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the normalized product name, or an empty value for a status-only criterion.
     */
    public Optional<String> getProductName() {
        return productName;
    }

    /**
     * Returns the immutable set of accepted statuses.
     */
    public Set<ItemStatus> getStatuses() {
        return statuses;
    }

    @Override
    public boolean test(Person person) {
        return getItems(person).stream().anyMatch(this::matches);
    }

    /**
     * Returns whether this single item satisfies every item condition.
     */
    public boolean matches(Item item) {
        requireNonNull(item);
        boolean isMatchingProduct = productName.isEmpty()
                || productName.get().equals(normalizeProductName(item.getName().value));
        return isMatchingProduct && statuses.contains(item.getStatus());
    }

    /**
     * Returns an unmodifiable list of matching items, preserving their order and repeated entries.
     * Customers without a preorder or with an empty preorder have no matching items.
     */
    public List<Item> getMatchingItems(Person person) {
        return getItems(person).stream().filter(this::matches).toList();
    }

    private List<Item> getItems(Person person) {
        return requireNonNull(person).getPreorder().map(Preorder::getItems).orElse(List.of());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof ItemCriterion otherCriterion)) {
            return false;
        }
        return productName.equals(otherCriterion.productName) && statuses.equals(otherCriterion.statuses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productName, statuses);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("productName", productName).add("statuses", statuses).toString();
    }
}
