package seedu.address.testutil;

import java.util.Optional;

import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.PreorderStatus;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.preorder.Remark;

/**
 * A utility class to help with building Item objects.
 */
public class ItemBuilder {

    public static final String DEFAULT_NAME = "Pineapple Tarts";
    public static final String DEFAULT_QUANTITY = "2";
    public static final PreorderStatus DEFAULT_STATUS = PreorderStatus.PENDING;

    private ItemName name;
    private Quantity quantity;
    private PreorderStatus status;
    private Optional<Remark> remark;

    /**
     * Creates an {@code ItemBuilder} with the default details and no remark.
     */
    public ItemBuilder() {
        name = new ItemName(DEFAULT_NAME);
        quantity = new Quantity(DEFAULT_QUANTITY);
        status = DEFAULT_STATUS;
        remark = Optional.empty();
    }

    /**
     * Initializes the ItemBuilder with the data of {@code itemToCopy}.
     */
    public ItemBuilder(Item itemToCopy) {
        name = itemToCopy.getName();
        quantity = itemToCopy.getQuantity();
        status = itemToCopy.getStatus();
        remark = itemToCopy.getRemark();
    }

    /**
     * Sets the {@code ItemName} of the {@code Item} that we are building.
     */
    public ItemBuilder withName(String name) {
        this.name = new ItemName(name);
        return this;
    }

    /**
     * Sets the {@code Quantity} of the {@code Item} that we are building.
     */
    public ItemBuilder withQuantity(String quantity) {
        this.quantity = new Quantity(quantity);
        return this;
    }

    /**
     * Sets the {@code PreorderStatus} of the {@code Item} that we are building.
     */
    public ItemBuilder withStatus(PreorderStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the {@code Remark} of the {@code Item} that we are building.
     */
    public ItemBuilder withRemark(String remark) {
        this.remark = Optional.of(new Remark(remark));
        return this;
    }

    public Item build() {
        return new Item(name, quantity, status, remark);
    }

}
