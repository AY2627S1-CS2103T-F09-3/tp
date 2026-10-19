package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class PostalCodeTest {
    @Test
    public void constructor_validPostalCode_preservesValue() {
        PostalCode postalCode = new PostalCode("012345");
        assertEquals("012345", postalCode.value);
    }

    @Test
    public void constructor_invalidPostalCode_throws() {
        assertThrows(IllegalArgumentException.class, () -> new PostalCode("12345"));
        assertThrows(IllegalArgumentException.class, () -> new PostalCode("1234567"));
        assertThrows(IllegalArgumentException.class, () -> new PostalCode("12A456"));
        assertThrows(IllegalArgumentException.class, () -> new PostalCode("      "));
    }

    @Test
    public void isValidPostalCode() {
        assertTrue(PostalCode.isValidPostalCode("123456"));
        assertThrows(NullPointerException.class, () -> PostalCode.isValidPostalCode(null));
    }
}
