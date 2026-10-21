package seedu.address.model.preorder;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a customer's current preorder, made up of the items they ordered and the fulfilment
 * details shared by all of those items.
 * Guarantees: immutable; items are not null and keep the order they were added in.
 */
public class Preorder {

    private final List<Item> items;
    private final FulfilmentDate date; // null when no date has been set
    private final FulfilmentMethod method; // null when no method has been set
    private final HandoverNotes handoverNotes; // null when there are no handover notes

    /**
     * Creates a preorder with the given items and no fulfilment details. A preorder may have no items yet.
     *
     * @throws NullPointerException if {@code items} or any of its elements is null.
     */
    public Preorder(List<Item> items) {
        this(items, Optional.empty(), Optional.empty(), Optional.empty());
    }

    /**
     * Creates a preorder with the given items and fulfilment details. A preorder may have no items yet.
     * Pass {@code Optional.empty()} for any fulfilment detail that has not been set.
     *
     * @throws NullPointerException if any argument, or any element of {@code items}, is null.
     */
    public Preorder(List<Item> items, Optional<FulfilmentDate> date, Optional<FulfilmentMethod> method,
            Optional<HandoverNotes> handoverNotes) {
        requireAllNonNull(items, date, method, handoverNotes);
        this.items = List.copyOf(items);
        this.date = date.orElse(null);
        this.method = method.orElse(null);
        this.handoverNotes = handoverNotes.orElse(null);
    }

    /**
     * Returns an immutable list of the items, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * Returns the date the preorder is to be delivered or collected, or an empty {@code Optional} if it is not set.
     */
    public Optional<FulfilmentDate> getDate() {
        return Optional.ofNullable(date);
    }

    /**
     * Returns how the customer receives the preorder, or an empty {@code Optional} if it is not set.
     */
    public Optional<FulfilmentMethod> getMethod() {
        return Optional.ofNullable(method);
    }

    /**
     * Returns the handover notes, or an empty {@code Optional} if there are none.
     */
    public Optional<HandoverNotes> getHandoverNotes() {
        return Optional.ofNullable(handoverNotes);
    }

    /**
     * Returns a copy of this preorder with {@code newItems} as its items and the same fulfilment details.
     *
     * @throws NullPointerException if {@code newItems} or any of its elements is null.
     */
    public Preorder withItems(List<Item> newItems) {
        return new Preorder(newItems, getDate(), getMethod(), getHandoverNotes());
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

        return items.equals(otherPreorder.items)
                && Objects.equals(date, otherPreorder.date)
                && method == otherPreorder.method
                && Objects.equals(handoverNotes, otherPreorder.handoverNotes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, date, method, handoverNotes);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("items", items)
                .add("date", getDate())
                .add("method", getMethod())
                .add("handoverNotes", getHandoverNotes())
                .toString();
    }

}
