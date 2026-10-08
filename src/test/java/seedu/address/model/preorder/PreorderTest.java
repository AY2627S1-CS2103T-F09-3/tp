package seedu.address.model.preorder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PreorderTest {

    private static final PreorderDetails CAKES = new PreorderDetails("2 chocolate cakes");
    private static final PreorderDetails TARTS = new PreorderDetails("50 pineapple tarts");

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Preorder(null, PreorderStatus.PENDING));
        assertThrows(NullPointerException.class, () -> new Preorder(CAKES, null));
    }

    @Test
    public void getters_returnGivenValues() {
        Preorder preorder = new Preorder(CAKES, PreorderStatus.PREPARED);
        assertEquals(CAKES, preorder.getDetails());
        assertEquals(PreorderStatus.PREPARED, preorder.getStatus());
    }

    @Test
    public void equals() {
        Preorder preorder = new Preorder(CAKES, PreorderStatus.PENDING);

        // same values -> returns true
        assertTrue(preorder.equals(new Preorder(new PreorderDetails("2 chocolate cakes"), PreorderStatus.PENDING)));

        // same object -> returns true
        assertTrue(preorder.equals(preorder));

        // null -> returns false
        assertFalse(preorder.equals(null));

        // different types -> returns false
        assertFalse(preorder.equals(5));

        // different details -> returns false
        assertFalse(preorder.equals(new Preorder(TARTS, PreorderStatus.PENDING)));

        // different status -> returns false
        assertFalse(preorder.equals(new Preorder(CAKES, PreorderStatus.COMPLETED)));
    }

    @Test
    public void hashCode_sameValues_sameHashCode() {
        assertEquals(new Preorder(CAKES, PreorderStatus.PENDING).hashCode(),
                new Preorder(CAKES, PreorderStatus.PENDING).hashCode());
    }

    @Test
    public void toStringMethod() {
        Preorder preorder = new Preorder(CAKES, PreorderStatus.PENDING);
        String expected = Preorder.class.getCanonicalName() + "{details=" + CAKES + ", status=PENDING}";
        assertEquals(expected, preorder.toString());
    }
}
