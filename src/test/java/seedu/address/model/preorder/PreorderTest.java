package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.ItemBuilder;

public class PreorderTest {

    private static final Item TARTS = new ItemBuilder().withName("Pineapple Tarts").build();
    private static final Item COOKIES = new ItemBuilder().withName("Almond Cookies").withQuantity("1")
            .withStatus(PreorderStatus.PREPARED).build();

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Preorder(null));
        assertThrows(NullPointerException.class, () -> new Preorder(Arrays.asList(TARTS, null)));
    }

    @Test
    public void getItems_returnsItemsInOrder() {
        assertEquals(List.of(TARTS, COOKIES), new Preorder(List.of(TARTS, COOKIES)).getItems());
    }

    @Test
    public void getItems_noItems_returnsEmptyList() {
        assertTrue(new Preorder(List.of()).getItems().isEmpty());
    }

    @Test
    public void getItems_modifyList_throwsUnsupportedOperationException() {
        Preorder preorder = new Preorder(List.of(TARTS));
        assertThrows(UnsupportedOperationException.class, () -> preorder.getItems().add(COOKIES));
    }

    @Test
    public void constructor_sourceListModified_preorderUnchanged() {
        List<Item> items = new ArrayList<>(List.of(TARTS));
        Preorder preorder = new Preorder(items);

        items.add(COOKIES);

        assertEquals(List.of(TARTS), preorder.getItems());
    }

    @Test
    public void equals() {
        Preorder preorder = new Preorder(List.of(TARTS, COOKIES));

        // same items -> returns true
        assertTrue(preorder.equals(new Preorder(List.of(TARTS, COOKIES))));

        // same object -> returns true
        assertTrue(preorder.equals(preorder));

        // null -> returns false
        assertFalse(preorder.equals(null));

        // different types -> returns false
        assertFalse(preorder.equals(5));

        // same items in a different order -> returns false
        assertFalse(preorder.equals(new Preorder(List.of(COOKIES, TARTS))));

        // fewer items -> returns false
        assertFalse(preorder.equals(new Preorder(List.of(TARTS))));

        // no items -> returns false
        assertFalse(preorder.equals(new Preorder(List.of())));
    }

    @Test
    public void hashCode_sameItems_sameHashCode() {
        assertEquals(new Preorder(List.of(TARTS, COOKIES)).hashCode(),
                new Preorder(List.of(TARTS, COOKIES)).hashCode());
    }

    @Test
    public void toStringMethod() {
        Preorder preorder = new Preorder(List.of(TARTS));
        String expected = Preorder.class.getCanonicalName() + "{items=" + List.of(TARTS) + "}";
        assertEquals(expected, preorder.toString());
    }
}
