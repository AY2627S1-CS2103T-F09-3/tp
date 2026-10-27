package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedItem.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.preorder.Remark;
import seedu.address.testutil.ItemBuilder;

public class JsonAdaptedItemTest {

    private static final String VALID_NAME = "Pineapple Tarts";
    private static final String VALID_QUANTITY = "2";
    private static final String VALID_STATUS = "PENDING";
    private static final String VALID_REMARK = "Less sugar";

    @Test
    public void toModelType_itemWithRemark_returnsItem() throws Exception {
        Item item = new ItemBuilder().withRemark(VALID_REMARK).build();
        assertEquals(item, new JsonAdaptedItem(item).toModelType());
    }

    @Test
    public void toModelType_itemWithoutRemark_returnsItem() throws Exception {
        Item item = new ItemBuilder().withStatus(ItemStatus.COMPLETED).build();
        assertEquals(item, new JsonAdaptedItem(item).toModelType());
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedItem item = new JsonAdaptedItem(null, VALID_QUANTITY, VALID_STATUS, VALID_REMARK);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, ItemName.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, item::toModelType);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedItem item = new JsonAdaptedItem(" ", VALID_QUANTITY, VALID_STATUS, VALID_REMARK);
        assertThrows(IllegalValueException.class, ItemName.MESSAGE_CONSTRAINTS, item::toModelType);
        JsonAdaptedItem unicodeSpaceItem = new JsonAdaptedItem("\u2003", VALID_QUANTITY, VALID_STATUS, VALID_REMARK);
        assertThrows(IllegalValueException.class, ItemName.MESSAGE_CONSTRAINTS, unicodeSpaceItem::toModelType);
    }

    @Test
    public void toModelType_nameWithRepeatedSpaces_loadedWithOneSpace() throws Exception {
        // Names saved before spaces were collapsed load as the same product as newer ones
        JsonAdaptedItem item = new JsonAdaptedItem("Pineapple   Tarts", VALID_QUANTITY, VALID_STATUS, null);
        assertEquals("Pineapple Tarts", item.toModelType().getName().value);
    }

    @Test
    public void toModelType_itemWithoutQuantity_returnsItem() throws Exception {
        Item item = new ItemBuilder().withoutQuantity().build();
        assertEquals(item, new JsonAdaptedItem(item).toModelType());
    }

    @Test
    public void toModelType_invalidQuantity_throwsIllegalValueException() {
        JsonAdaptedItem item = new JsonAdaptedItem(VALID_NAME, "0", VALID_STATUS, VALID_REMARK);
        assertThrows(IllegalValueException.class, Quantity.MESSAGE_CONSTRAINTS, item::toModelType);
    }

    @Test
    public void toModelType_nullStatus_throwsIllegalValueException() {
        JsonAdaptedItem item = new JsonAdaptedItem(VALID_NAME, VALID_QUANTITY, null, VALID_REMARK);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, ItemStatus.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, item::toModelType);
    }

    @Test
    public void toModelType_invalidStatus_throwsIllegalValueException() {
        JsonAdaptedItem item = new JsonAdaptedItem(VALID_NAME, VALID_QUANTITY, "DONE", VALID_REMARK);
        assertThrows(IllegalValueException.class, ItemStatus.MESSAGE_CONSTRAINTS, item::toModelType);
    }

    @Test
    public void toModelType_invalidRemark_throwsIllegalValueException() {
        JsonAdaptedItem item = new JsonAdaptedItem(VALID_NAME, VALID_QUANTITY, VALID_STATUS, "Less sugar\nNo nuts");
        assertThrows(IllegalValueException.class, Remark.MESSAGE_CONSTRAINTS, item::toModelType);
    }
}
