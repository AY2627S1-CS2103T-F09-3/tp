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
    private final long totalQuantity;

    private ProductPreparationSummary(List<MatchingItem> matchingItems) {
        this.matchingItems = List.copyOf(matchingItems);
        customerCount = matchingItems.stream().map(MatchingItem::getCustomer).distinct().count();
        totalQuantity = matchingItems.stream().mapToLong(match -> match.getItem().getQuantity().value).sum();
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

    public long getTotalQuantity() {
        return this.totalQuantity;
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
