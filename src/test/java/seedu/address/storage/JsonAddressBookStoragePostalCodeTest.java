package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.PostalCode;

public class JsonAddressBookStoragePostalCodeTest {
    private static final String PERSON_JSON = """
            {
              "name": "%s",
              "phone": "91234567",
              "email": "customer@example.com",
              "address": "123 Test Road",
              %s
              "tags": []
            }
            """;

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_missingPostalCode_rejectsWithoutChangingFile() throws Exception {
        assertRejectedWithoutChangingFile(
                "Person's PostalCode field is missing!", PERSON_JSON.formatted("Postal Customer", ""));
    }

    @Test
    public void readAddressBook_nullPostalCode_rejectsWithoutChangingFile() throws Exception {
        assertRejectedWithoutChangingFile("Person's PostalCode field is missing!",
                PERSON_JSON.formatted("Postal Customer", "\"postalCode\": null,"));
    }

    @Test
    public void readAddressBook_invalidPostalCode_rejectsWithoutChangingFile() throws Exception {
        for (String value : new String[]{"", "   ", "null", "0", "01234", "0123456", "01A345", "-12345"}) {
            String person = PERSON_JSON.formatted("Postal Customer", "\"postalCode\": \"" + value + "\",");
            assertRejectedWithoutChangingFile(PostalCode.MESSAGE_CONSTRAINTS, person);
        }
    }

    @Test
    public void readAddressBook_invalidCustomerAfterValidCustomer_rejectsWholeFileWithoutChangingIt() throws Exception {
        String valid = PERSON_JSON.formatted("Alice", "\"postalCode\": \"012345\",");
        String missing = PERSON_JSON.formatted("Bob", "");
        assertRejectedWithoutChangingFile("Person's PostalCode field is missing!", valid, missing);
    }

    @Test
    public void readAndSaveAddressBook_validPostalCodes_preservesAllDigits() throws Exception {
        Path file = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (String code : new String[]{"000000", "000001", "012345", "999999"}) {
            Files.writeString(file, addressBookJson(
                    PERSON_JSON.formatted("Postal Customer", "\"postalCode\": \"" + code + "\",")));
            ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
            assertEquals(code, loaded.getPersonList().getFirst().getPostalCode().value);

            storage.saveAddressBook(loaded);

            ReadOnlyAddressBook reloaded = storage.readAddressBook().orElseThrow();
            assertEquals(loaded, reloaded);
            assertEquals(code, reloaded.getPersonList().getFirst().getPostalCode().value);
        }
    }

    private void assertRejectedWithoutChangingFile(String expectedMessage, String... persons) throws Exception {
        Path file = testFolder.resolve("addressbook.json");
        Files.writeString(file, addressBookJson(persons));
        byte[] original = Files.readAllBytes(file);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);

        DataLoadingException exception = assertThrows(DataLoadingException.class, storage::readAddressBook);

        assertEquals(expectedMessage, exception.getCause().getMessage());
        assertArrayEquals(original, Files.readAllBytes(file));
    }

    private static String addressBookJson(String... persons) {
        return "{\"persons\": [" + String.join(",", persons) + "]}";
    }
}
