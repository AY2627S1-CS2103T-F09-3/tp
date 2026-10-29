package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.preorder.Quantity;

public class JsonAddressBookStorageQuantityTest {
    private static final String PERSON_JSON = """
            {
              "name": "%s",
              "phone": "91234567",
              "email": "customer@example.com",
              "address": "123 Test Road",
              "postalCode": "012345",
              "tags": [],
              %s
            }
            """;
    private static final String PREORDER_JSON = """
            "preorders": [ { "items": [ { "name": "Pineapple Tarts", %s "status": "PENDING" } ] } ]
            """;
    private static final String MISSING_QUANTITY_MESSAGE = "Item's Quantity field is missing!";

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_missingQuantity_rejectsWithoutChangingFile() throws Exception {
        assertRejectedWithoutChangingFile(MISSING_QUANTITY_MESSAGE, personWithQuantity("Quantity Customer", ""));
    }

    @Test
    public void readAddressBook_nullQuantity_rejectsWithoutChangingFile() throws Exception {
        assertRejectedWithoutChangingFile(MISSING_QUANTITY_MESSAGE,
                personWithQuantity("Quantity Customer", "\"quantity\": null,"));
    }

    @Test
    public void readAddressBook_invalidQuantity_rejectsWithoutChangingFile() throws Exception {
        for (String value : new String[]{"", "   ", "null", "0", "007", "1000", "-1", "1.5", "two"}) {
            String person = personWithQuantity("Quantity Customer", "\"quantity\": \"" + value + "\",");
            assertRejectedWithoutChangingFile(Quantity.MESSAGE_CONSTRAINTS, person);
        }
    }

    @Test
    public void readAddressBook_missingQuantityAfterValidCustomer_rejectsWholeFileWithoutChangingIt()
            throws Exception {
        String valid = personWithQuantity("Alice", "\"quantity\": \"2\",");
        String missing = personWithQuantity("Bob", "");
        assertRejectedWithoutChangingFile(MISSING_QUANTITY_MESSAGE, valid, missing);
    }

    @Test
    public void readAddressBook_v13Preorder_rejectsWithoutChangingFile() throws Exception {
        String legacyPreorder = "\"preorder\": { \"details\": \"2 chocolate cakes\", \"status\": \"PENDING\" }";
        assertRejectedWithoutChangingFile(JsonAdaptedPerson.MESSAGE_LEGACY_PREORDER,
                PERSON_JSON.formatted("Legacy Customer", legacyPreorder));
    }

    @Test
    public void readAddressBook_v13PreorderBesideCurrentPreorders_rejectsWithoutChangingFile() throws Exception {
        String bothFormats = "\"preorders\": [],"
                + " \"preorder\": { \"details\": \"2 chocolate cakes\", \"status\": \"PENDING\" }";
        assertRejectedWithoutChangingFile(JsonAdaptedPerson.MESSAGE_LEGACY_PREORDER,
                PERSON_JSON.formatted("Legacy Customer", bothFormats));
    }

    @Test
    public void readAndSaveAddressBook_validQuantities_preservesQuantity() throws Exception {
        Path file = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        for (String quantity : new String[]{"1", "2", "999"}) {
            Files.writeString(file, addressBookJson(
                    personWithQuantity("Quantity Customer", "\"quantity\": \"" + quantity + "\",")));
            ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();
            assertEquals(quantity, getFirstQuantity(loaded));

            storage.saveAddressBook(loaded);

            ReadOnlyAddressBook reloaded = storage.readAddressBook().orElseThrow();
            assertEquals(loaded, reloaded);
            assertEquals(quantity, getFirstQuantity(reloaded));
        }
    }

    @Test
    public void saveAddressBook_customerWithPreorder_savesOnlyCurrentFields() throws Exception {
        Path file = testFolder.resolve("addressbook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
        Files.writeString(file, addressBookJson(personWithQuantity("Quantity Customer", "\"quantity\": \"2\",")));

        storage.saveAddressBook(storage.readAddressBook().orElseThrow());

        JsonNode person = JsonUtil.readJsonFile(file, JsonNode.class).orElseThrow().get("persons").get(0);
        List<String> fieldNames = new ArrayList<>();
        person.fieldNames().forEachRemaining(fieldNames::add);
        assertEquals(List.of("name", "phone", "email", "address", "postalCode", "tags", "preorders"), fieldNames);
        assertEquals("2", person.get("preorders").get(0).get("items").get(0).get("quantity").asText());
    }

    private static String getFirstQuantity(ReadOnlyAddressBook addressBook) {
        return addressBook.getPersonList().getFirst().getPreorder().orElseThrow()
                .getItems().getFirst().getQuantity().toString();
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

    private static String personWithQuantity(String name, String quantityField) {
        return PERSON_JSON.formatted(name, PREORDER_JSON.formatted(quantityField));
    }

    private static String addressBookJson(String... persons) {
        return "{\"persons\": [" + String.join(",", persons) + "]}";
    }
}
