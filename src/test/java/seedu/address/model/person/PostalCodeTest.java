package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class PostalCodeTest {
    @Test
    public void constructor_validPostalCode_preservesValue() {
        PostalCode postalCode = new PostalCode("012345");
        assertEquals("012345", postalCode.value);
    }

    @Test
    public void constructor_sixDigitBoundaries_preservesAllDigits() {
        for (String value : new String[]{"000000", "000001", "999999"}) {
            assertTrue(PostalCode.isValidPostalCode(value));
            PostalCode postalCode = new PostalCode(value);
            assertEquals(value, postalCode.value);
            assertEquals(value, postalCode.toString());
        }
        assertFalse(PostalCode.isValidPostalCode("00000"));
        assertFalse(PostalCode.isValidPostalCode("1000000"));
    }

    @Test
    public void equals_comparesPostalValuesAndTypes() {
        PostalCode postalCode = new PostalCode("012345");
        assertTrue(postalCode.equals(postalCode));
        assertTrue(postalCode.equals(new PostalCode("012345")));
        assertFalse(postalCode.equals(new PostalCode("123450")));
        assertFalse(postalCode.equals(null));
        assertFalse(postalCode.equals("012345"));
    }

    @Test
    public void hashCode_equalPostalCodes_areInterchangeableInSet() {
        Set<PostalCode> postalCodes = new HashSet<>();
        postalCodes.add(new PostalCode("012345"));
        assertFalse(postalCodes.add(new PostalCode("012345")));
        assertTrue(postalCodes.contains(new PostalCode("012345")));
        assertFalse(postalCodes.contains(new PostalCode("123450")));
        assertEquals(1, postalCodes.size());
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
