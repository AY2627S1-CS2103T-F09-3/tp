package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.ItemBuilder;

public class ItemTest {

    private static final ItemName TARTS = new ItemName("Pineapple Tarts");
    private static final Optional<Quantity> TWO = Optional.of(new Quantity("2"));

    @Test
    public void constructor_null_throwsNullPointerException() {
        Optional<Remark> noRemark = Optional.empty();
        assertThrows(NullPointerException.class, () -> new Item(null, TWO, ItemStatus.PENDING, noRemark));
        assertThrows(NullPointerException.class, () -> new Item(TARTS, null, ItemStatus.PENDING, noRemark));
        assertThrows(NullPointerException.class, () -> new Item(TARTS, TWO, null, noRemark));
        assertThrows(NullPointerException.class, () -> new Item(TARTS, TWO, ItemStatus.PENDING, null));
    }

    @Test
    public void getters_returnGivenValues() {
        Remark lessSugar = new Remark("Less sugar");
        Item item = new Item(TARTS, TWO, ItemStatus.PREPARED, Optional.of(lessSugar));

        assertEquals(TARTS, item.getName());
        assertEquals(TWO, item.getQuantity());
        assertEquals(ItemStatus.PREPARED, item.getStatus());
        assertEquals(Optional.of(lessSugar), item.getRemark());
    }

    @Test
    public void getQuantity_convertedWithoutQuantity_returnsEmpty() {
        assertEquals(Optional.empty(), new ItemBuilder().withoutQuantity().build().getQuantity());
    }

    @Test
    public void getRemark_noRemark_returnsEmpty() {
        assertEquals(Optional.empty(), new ItemBuilder().build().getRemark());
    }

    @Test
    public void equals() {
        Item item = new ItemBuilder().withRemark("Less sugar").build();

        // same values -> returns true
        assertTrue(item.equals(new ItemBuilder(item).build()));

        // same object -> returns true
        assertTrue(item.equals(item));

        // null -> returns false
        assertFalse(item.equals(null));

        // different types -> returns false
        assertFalse(item.equals(5));

        // different name -> returns false
        assertFalse(item.equals(new ItemBuilder(item).withName("Almond Cookies").build()));

        // different quantity -> returns false
        assertFalse(item.equals(new ItemBuilder(item).withQuantity("3").build()));

        // no quantity -> returns false
        assertFalse(item.equals(new ItemBuilder(item).withoutQuantity().build()));

        // different status -> returns false
        assertFalse(item.equals(new ItemBuilder(item).withStatus(ItemStatus.COMPLETED).build()));

        // different remark -> returns false
        assertFalse(item.equals(new ItemBuilder(item).withRemark("Separate packaging").build()));

        // no remark -> returns false
        assertFalse(item.equals(new ItemBuilder().build()));
    }

    @Test
    public void hashCode_sameValues_sameHashCode() {
        Item item = new ItemBuilder().withRemark("Less sugar").build();
        assertEquals(item.hashCode(), new ItemBuilder(item).build().hashCode());
    }

    @Test
    public void toStringMethod() {
        Item item = new ItemBuilder().build();
        String expected = Item.class.getCanonicalName() + "{name=" + item.getName() + ", quantity="
                + item.getQuantity() + ", status=" + item.getStatus() + ", remark=" + item.getRemark() + "}";
        assertEquals(expected, item.toString());
    }
}
