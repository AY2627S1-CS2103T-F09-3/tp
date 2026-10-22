package seedu.address.model.summary;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.search.ItemCriterion;

/**
 * An immutable snapshot of one product's matching items among the displayed customers.
 * Quantities are summed only for that product and the criterion's accepted statuses.
 */
public final class ProductPreparationSummary {
    private final List<MatchingItem> matchingItems;
    private final long customerCount;
    private final long knownQuantity;
    // Migrated v1.3 items may lack quantities. Track them separately so the known subtotal
    // is not presented as a complete total when some quantities still need to be entered.
    private final long missingQuantityCount;

    private ProductPreparationSummary(List<MatchingItem> matchingItems) {
        this.matchingItems = List.copyOf(matchingItems);
        customerCount = matchingItems.stream().map(MatchingItem::getCustomer).distinct().count();
        knownQuantity = matchingItems.stream()
                .mapToLong(match -> match.getItem().getQuantity().map(quantity -> (long) quantity.value).orElse(0L))
                .sum();
        missingQuantityCount = matchingItems.stream().filter(match -> match.getItem().getQuantity().isEmpty()).count();
    }

    /**
     * Summarizes only the supplied customers, using the shared matcher for item selection.
     * Callers supply the currently displayed customers so other active criteria are respected.
     * Repeated customers are counted once; repeated item entries within a preorder remain separate.
     *
     * @throws NullPointerException If an argument or a customer is null.
     * @throws IllegalArgumentException If the criterion has no selected product.
     */
    public static ProductPreparationSummary from(List<Person> customers, ItemCriterion criterion) {
        requireNonNull(customers);
        requireNonNull(criterion);
        checkArgument(criterion.getProductName().isPresent(), "A preparation summary requires a product search.");

        List<Person> customerSnapshot = List.copyOf(customers);
        List<MatchingItem> matches = new ArrayList<>();
        customerSnapshot.stream().distinct().forEach(customer -> addMatchingItems(matches, customer, criterion));
        return new ProductPreparationSummary(matches);
    }

    private static void addMatchingItems(List<MatchingItem> matches, Person customer, ItemCriterion criterion) {
        List<Item> items = customer.getPreorder().map(Preorder::getItems).orElse(List.of());
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (criterion.matches(item)) {
                matches.add(new MatchingItem(customer, i + 1, item));
            }
        }
    }

    public long getCustomerCount() {
        return this.customerCount;
    }

    public int getMatchingItemCount() {
        return this.matchingItems.size();
    }

    /**
     * Returns the subtotal of known quantities. Check missing quantities before presenting it as a total.
     */
    public long getKnownQuantity() {
        return this.knownQuantity;
    }

    public long getMissingQuantityCount() {
        return this.missingQuantityCount;
    }

    /**
     * Returns unmodifiable matches in customer and item order, retaining each item's quantity and optional remark.
     */
    public List<MatchingItem> getMatchingItems() {
        return this.matchingItems;
    }

    /**
     * Associates an item with its customer and original one-based item number, even when other items are excluded.
     * Instances are produced from the customer's preorder by the summary factory.
     */
    public static final class MatchingItem {
        private final Person customer;
        private final int itemNumber;
        private final Item item;

        /**
         * Validates that the item reference identifies the stated entry in the customer's preorder.
         */
        public MatchingItem(Person customer, int itemNumber, Item item) {
            requireNonNull(customer);
            requireNonNull(item);

            List<Item> items = customer.getPreorder().map(Preorder::getItems).orElse(List.of());
            checkArgument(itemNumber > 0 && itemNumber <= items.size(), "Item number must identify a preorder item.");
            checkArgument(items.get(itemNumber - 1).equals(item), "Item must match the numbered preorder entry.");
            this.customer = customer;
            this.itemNumber = itemNumber;
            this.item = item;
        }

        public Person getCustomer() {
            return this.customer;
        }

        public int getItemNumber() {
            return this.itemNumber;
        }

        public Item getItem() {
            return this.item;
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }
            if (!(other instanceof MatchingItem otherMatch)) {
                return false;
            }
            return customer.equals(otherMatch.customer)
                    && itemNumber == otherMatch.itemNumber
                    && item.equals(otherMatch.item);
        }

        @Override
        public int hashCode() {
            return Objects.hash(customer, itemNumber, item);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).add("customer", customer)
                    .add("itemNumber", itemNumber).add("item", item).toString();
        }
    }
}
