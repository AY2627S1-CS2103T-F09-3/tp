package seedu.address.model.preorder;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents one product in a customer's preorder, such as 2 boxes of pineapple tarts.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Item {

    private final ItemName name;
    private final Quantity quantity; // null only for items converted from v1.3, which had no quantity
    private final ItemStatus status;
    private final Remark remark; // null when the customer made no special request

    /**
     * Every field must be present and not null.
     * Pass {@code Optional.empty()} as {@code remark} for an item without a special request.
     * New items must have a quantity; {@code Optional.empty()} as {@code quantity} is only for
     * items converted from a v1.3 preorder, which did not record one.
     */
    public Item(ItemName name, Optional<Quantity> quantity, ItemStatus status, Optional<Remark> remark) {
        requireAllNonNull(name, quantity, status, remark);
        this.name = name;
        this.quantity = quantity.orElse(null);
        this.status = status;
        this.remark = remark.orElse(null);
    }

    public ItemName getName() {
        return name;
    }

    /**
     * Returns the item's quantity, or an empty {@code Optional} if it was converted from v1.3 without one.
     */
    public Optional<Quantity> getQuantity() {
        return Optional.ofNullable(quantity);
    }

    public ItemStatus getStatus() {
        return status;
    }

    /**
     * Returns the item's remark, or an empty {@code Optional} if there is none.
     */
    public Optional<Remark> getRemark() {
        return Optional.ofNullable(remark);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Item otherItem)) {
            return false;
        }

        return name.equals(otherItem.name)
                && Objects.equals(quantity, otherItem.quantity)
                && status == otherItem.status
                && Objects.equals(remark, otherItem.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, status, remark);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("quantity", getQuantity())
                .add("status", status)
                .add("remark", getRemark())
                .toString();
    }

}
