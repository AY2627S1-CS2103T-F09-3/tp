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
        for (String invalid : new String[]{"", "12345", "1234567", "12A456", "      ", "123 45", "-12345",
            "123.45", " 123456", "123456 ", "\u0661\u0662\u0663\u0664\u0665\u0666"}) {
            assertThrows(IllegalArgumentException.class, () -> new PostalCode(invalid));
        }
        assertThrows(NullPointerException.class, () -> new PostalCode(null));
    }

    @Test
    public void isValidPostalCode() {
        assertTrue(PostalCode.isValidPostalCode("123456"));
        assertThrows(NullPointerException.class, () -> PostalCode.isValidPostalCode(null));
    }
}
