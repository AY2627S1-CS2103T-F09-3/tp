package seedu.address.storage;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.preorder.Remark;

/**
 * Jackson-friendly version of {@link Item}.
 */
class JsonAdaptedItem {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Item's %s field is missing!";

    private final String name;
    private final String quantity;
    private final String status;
    private final String remark; // null when the item has no remark

    /**
     * Constructs a {@code JsonAdaptedItem} with the given item details.
     */
    @JsonCreator
    public JsonAdaptedItem(@JsonProperty("name") String name, @JsonProperty("quantity") String quantity,
            @JsonProperty("status") String status, @JsonProperty("remark") String remark) {
        this.name = name;
        this.quantity = quantity;
        this.status = status;
        this.remark = remark;
    }

    /**
     * Converts a given {@code Item} into this class for Jackson use.
     */
    public JsonAdaptedItem(Item source) {
        name = source.getName().value;
        quantity = source.getQuantity().toString();
        status = source.getStatus().name();
        remark = source.getRemark().map(itemRemark -> itemRemark.value).orElse(null);
    }

    /**
     * Converts this Jackson-friendly adapted item object into the model's {@code Item} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted item.
     */
    public Item toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    ItemName.class.getSimpleName()));
        }
        if (!ItemName.isValidItemName(name)) {
            throw new IllegalValueException(ItemName.MESSAGE_CONSTRAINTS);
        }

        if (quantity == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    Quantity.class.getSimpleName()));
        }
        if (!Quantity.isValidQuantity(quantity)) {
            throw new IllegalValueException(Quantity.MESSAGE_CONSTRAINTS);
        }

        if (status == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    ItemStatus.class.getSimpleName()));
        }
        if (!ItemStatus.isValidStatus(status)) {
            throw new IllegalValueException(ItemStatus.MESSAGE_CONSTRAINTS);
        }

        if (remark != null && !Remark.isValidRemark(remark)) {
            throw new IllegalValueException(Remark.MESSAGE_CONSTRAINTS);
        }

        ItemName itemName = new ItemName(name);
        Optional<Remark> itemRemark = Optional.ofNullable(remark).map(Remark::new);
        return new Item(itemName, new Quantity(quantity), ItemStatus.valueOf(status), itemRemark);
    }
}
