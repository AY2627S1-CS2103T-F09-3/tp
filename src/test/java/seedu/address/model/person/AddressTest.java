package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class AddressTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Address(null));
    }

    @Test
    public void constructor_invalidAddress_throwsIllegalArgumentException() {
        String invalidAddress = "";
        assertThrows(IllegalArgumentException.class, () -> new Address(invalidAddress));
    }

    @Test
    public void isValidAddress() {
        // null address
        assertThrows(NullPointerException.class, () -> Address.isValidAddress(null));

        // invalid addresses
        assertFalse(Address.isValidAddress("")); // empty string
        assertFalse(Address.isValidAddress(" ")); // spaces only
        assertFalse(Address.isValidAddress("Block123 s/pending")); // looks like a command prefix
        assertFalse(Address.isValidAddress("Block 123 x/foo")); // looks like an unsupported prefix
        assertFalse(Address.isValidAddress("s/pending Block 123")); // prefix-like text at the start
        assertFalse(Address.isValidAddress("w/o lift")); // only lowercase c/o is accepted as slash notation
        assertFalse(Address.isValidAddress("Block 123 c/O Alice")); // tokenizer recognizes only lowercase c/o

        // valid addresses
        assertTrue(Address.isValidAddress("Blk 456, Den Road, #01-355"));
        assertTrue(Address.isValidAddress("Block 123 c/o Alice #04/08"));
        assertTrue(Address.isValidAddress("-")); // one character
        assertTrue(Address.isValidAddress("Leng Inc; 1234 Market St; San Francisco CA 2349879; USA")); // long address
    }

    @Test
    public void isValidAddress_lengthBoundaries_returnsExpectedResult() {
        assertTrue(Address.isValidAddress("A".repeat(299)));
        assertTrue(Address.isValidAddress("A".repeat(300)));
        assertFalse(Address.isValidAddress("A".repeat(301)));
    }

    @Test
    public void constructor_lengthBoundaries_enforcesLimit() {
        for (int length : new int[]{299, 300}) {
            String address = "A".repeat(length);
            assertEquals(address, new Address(address).value);
        }
        assertThrows(IllegalArgumentException.class, Address.MESSAGE_LENGTH_CONSTRAINTS, ()
            -> new Address("A".repeat(301)));
    }

    @Test
    public void isValidAddress_supplementaryCharacters_countsUtf16CodeUnits() {
        // Each emoji occupies two UTF-16 code units.
        String emoji = "\uD83D\uDE00";
        assertTrue(Address.isValidAddress(emoji.repeat(149) + "A")); // 299 code units
        assertTrue(Address.isValidAddress(emoji.repeat(150))); // 300 code units
        assertFalse(Address.isValidAddress(emoji.repeat(150) + "A")); // 301 code units
    }

    @Test
    public void constructor_supplementaryCharacters_countsUtf16CodeUnits() {
        String emoji = "\uD83D\uDE00";
        for (String address : new String[]{emoji.repeat(149) + "A", emoji.repeat(150)}) {
            assertEquals(address, new Address(address).value);
        }
        String overLimitAddress = emoji.repeat(150) + "A";
        assertThrows(IllegalArgumentException.class, Address.MESSAGE_LENGTH_CONSTRAINTS, ()
            -> new Address(overLimitAddress));
    }

    @Test
    public void constructor_trailingSpacesAtLengthBoundary_countsStoredValue() {
        String address = "A".repeat(299) + " ";
        assertTrue(Address.isValidAddress(address));
        assertEquals(address, new Address(address).value);

        String overLimitAddress = address + " ";
        assertFalse(Address.isValidAddress(overLimitAddress));
        assertThrows(IllegalArgumentException.class, Address.MESSAGE_LENGTH_CONSTRAINTS, ()
            -> new Address(overLimitAddress));
    }

    @Test
    public void constructor_lengthAndFormatViolations_prioritizesLengthMessage() {
        for (int length : new int[]{299, 300}) {
            String invalidAddress = "A".repeat(length - 2) + "\nB";
            assertThrows(IllegalArgumentException.class, Address.MESSAGE_CONSTRAINTS, ()
                -> new Address(invalidAddress));
        }
        String overLimitAddress = "A".repeat(299) + "\nB";
        assertThrows(IllegalArgumentException.class, "Addresses must be at most 300 characters long.", ()
            -> new Address(overLimitAddress));
    }

    @Test
    public void equals() {
        Address address = new Address("Valid Address");

        // same values -> returns true
        assertTrue(address.equals(new Address("Valid Address")));

        // same object -> returns true
        assertTrue(address.equals(address));

        // null -> returns false
        assertFalse(address.equals(null));

        // different types -> returns false
        assertFalse(address.equals(5.0f));

        // different values -> returns false
        assertFalse(address.equals(new Address("Other Valid Address")));
    }
}
