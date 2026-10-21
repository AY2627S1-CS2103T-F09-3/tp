package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PreorderBuilder;

public class PreorderTest {

    private static final Item TARTS = new ItemBuilder().withName("Pineapple Tarts").build();
    private static final Item COOKIES = new ItemBuilder().withName("Almond Cookies").withQuantity("1")
            .withStatus(ItemStatus.PREPARED).build();
    private static final Preorder DELIVERY_PREORDER = new PreorderBuilder().withItems(TARTS)
            .withDate("2026-12-20").withMethod(FulfilmentMethod.DELIVERY).withHandoverNotes("Call on arrival")
            .build();

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Preorder(null));
        assertThrows(NullPointerException.class, () -> new Preorder(Arrays.asList(TARTS, null)));

        Optional<FulfilmentDate> date = DELIVERY_PREORDER.getDate();
        Optional<FulfilmentMethod> method = DELIVERY_PREORDER.getMethod();
        Optional<HandoverNotes> notes = DELIVERY_PREORDER.getHandoverNotes();
        assertThrows(NullPointerException.class, () -> new Preorder(null, date, method, notes));
        assertThrows(NullPointerException.class, () -> new Preorder(List.of(TARTS), null, method, notes));
        assertThrows(NullPointerException.class, () -> new Preorder(List.of(TARTS), date, null, notes));
        assertThrows(NullPointerException.class, () -> new Preorder(List.of(TARTS), date, method, null));
    }

    @Test
    public void constructor_itemsOnly_noFulfilmentDetails() {
        Preorder preorder = new Preorder(List.of(TARTS));
        assertTrue(preorder.getDate().isEmpty());
        assertTrue(preorder.getMethod().isEmpty());
        assertTrue(preorder.getHandoverNotes().isEmpty());
    }

    @Test
    public void getters_fulfilmentDetailsSet_returnsDetails() {
        assertEquals(Optional.of(new FulfilmentDate("2026-12-20")), DELIVERY_PREORDER.getDate());
        assertEquals(Optional.of(FulfilmentMethod.DELIVERY), DELIVERY_PREORDER.getMethod());
        assertEquals(Optional.of(new HandoverNotes("Call on arrival")), DELIVERY_PREORDER.getHandoverNotes());
    }

    @Test
    public void withItems_keepsFulfilmentDetails() {
        Preorder expected = new PreorderBuilder(DELIVERY_PREORDER).withItems(TARTS, COOKIES).build();
        assertEquals(expected, DELIVERY_PREORDER.withItems(List.of(TARTS, COOKIES)));
    }

    @Test
    public void withItems_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> DELIVERY_PREORDER.withItems(null));
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
    public void equals_fulfilmentDetails() {
        // same items and fulfilment details -> returns true
        assertTrue(DELIVERY_PREORDER.equals(new PreorderBuilder(DELIVERY_PREORDER).build()));

        // different date -> returns false
        assertFalse(DELIVERY_PREORDER.equals(new PreorderBuilder(DELIVERY_PREORDER).withDate("2026-12-21").build()));

        // different method -> returns false
        assertFalse(DELIVERY_PREORDER.equals(new PreorderBuilder(DELIVERY_PREORDER)
                .withMethod(FulfilmentMethod.COLLECTION).build()));

        // different handover notes -> returns false
        assertFalse(DELIVERY_PREORDER.equals(new PreorderBuilder(DELIVERY_PREORDER)
                .withHandoverNotes("Leave at door").build()));

        // same items without fulfilment details -> returns false
        assertFalse(DELIVERY_PREORDER.equals(new Preorder(List.of(TARTS))));
    }

    @Test
    public void hashCode_equalPreorders_sameHashCode() {
        assertEquals(new Preorder(List.of(TARTS, COOKIES)).hashCode(),
                new Preorder(List.of(TARTS, COOKIES)).hashCode());
        assertEquals(DELIVERY_PREORDER.hashCode(), new PreorderBuilder(DELIVERY_PREORDER).build().hashCode());
    }

    @Test
    public void toStringMethod() {
        String expected = Preorder.class.getCanonicalName() + "{items=" + List.of(TARTS)
                + ", date=" + DELIVERY_PREORDER.getDate()
                + ", method=" + DELIVERY_PREORDER.getMethod()
                + ", handoverNotes=" + DELIVERY_PREORDER.getHandoverNotes() + "}";
        assertEquals(expected, DELIVERY_PREORDER.toString());
    }
}
