package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;

import java.util.List;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a customer's current preorder, made up of the items they ordered.
 * Guarantees: immutable; items are not null and keep the order they were added in.
 */
public class Preorder {

    private final List<Item> items;

    /**
     * Creates a preorder with the given items. A preorder may have no items yet.
     *
     * @throws NullPointerException if {@code items} or any of its elements is null.
     */
    public Preorder(List<Item> items) {
        requireNonNull(items);
        this.items = List.copyOf(items);
    }

    /**
     * Returns an immutable list of the items, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public List<Item> getItems() {
        return items;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Preorder otherPreorder)) {
            return false;
        }

        return items.equals(otherPreorder.items);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("items", items)
                .toString();
    }

}
