package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PreorderBuilder;

public class JsonAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "JsonAddressBookStorageTest");
    private static final Path NAME_TEST_DATA_FOLDER =
            Paths.get("src", "test", "data", "JsonSerializableAddressBookTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new JsonAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.json").isPresent());
    }

    @Test
    public void read_notJsonFormat_exceptionThrown() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("notJsonFormatAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.json"));
    }

    @Test
    public void readAddressBook_invalidPerson_preservesOriginalFile() throws Exception {
        Path filePath = testFolder.resolve("invalid-person.json");
        Files.copy(TEST_DATA_FOLDER.resolve("invalidAndValidPersonAddressBook.json"), filePath);

        assertReadFailurePreservesFile(filePath);
    }

    @Test
    public void readAddressBook_nullJson_throwsWithoutChangingFile() throws Exception {
        Path filePath = testFolder.resolve("null.json");
        Files.writeString(filePath, "null");

        assertReadFailurePreservesFile(filePath);
    }

    @Test
    public void readAddressBook_nullPerson_throwsWithoutChangingFile() throws Exception {
        Path filePath = testFolder.resolve("null-person.json");
        Files.writeString(filePath, "{\"persons\":[null]}");

        assertReadFailurePreservesFile(filePath);
    }

    @Test
    public void readAddressBook_nullTag_throwsWithoutChangingFile() throws Exception {
        Path filePath = testFolder.resolve("null-tag.json");
        JsonAdaptedPerson person = new JsonAdaptedPerson(new PersonBuilder(ALICE).withTags("friends").build());
        String json = JsonUtil.toJsonString(new JsonSerializableAddressBook(List.of(person)));
        Files.writeString(filePath, json.replace("\"friends\"", "null"));

        assertReadFailurePreservesFile(filePath);
    }

    @Test
    public void readAddressBook_directory_throwsDataLoadingException() {
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertTrue(Files.isDirectory(testFolder));
    }

    @Test
    public void readAddressBook_validManualEdit_loadsWithoutChangingFile() throws Exception {
        Path filePath = testFolder.resolve("edited.json");
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);
        Files.writeString(filePath, Files.readString(filePath).replace(ALICE.getName().fullName, "Alice Baker"));
        byte[] editedContent = Files.readAllBytes(filePath);
        AddressBook expected = new AddressBook();
        expected.addPerson(new PersonBuilder(ALICE).withName("Alice Baker").build());

        assertEquals(expected, storage.readAddressBook().get());
        assertArrayEquals(editedContent, Files.readAllBytes(filePath));
    }

    private void assertReadFailurePreservesFile(Path filePath) throws Exception {
        byte[] originalContent = Files.readAllBytes(filePath);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
        assertArrayEquals(originalContent, Files.readAllBytes(filePath));
    }

    @Test
    public void readAddressBook_nameWithDigits_throwsDataLoadingException() {
        Path filePath = NAME_TEST_DATA_FOLDER.resolve("invalidNameAddressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void readAddressBook_caseAndSpacingDuplicateNames_throwsDataLoadingException() {
        Path filePath = NAME_TEST_DATA_FOLDER.resolve("caseAndSpacingDuplicatePersonAddressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        assertThrows(DataLoadingException.class, storage::readAddressBook);
    }

    @Test
    public void readAddressBook_missingItemName_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readPreorderFixture("missingItemName.json"));
    }

    @Test
    public void readAddressBook_missingItemStatus_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readPreorderFixture("missingItemStatus.json"));
    }

    @Test
    public void readAddressBook_invalidItemName_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readPreorderFixture("invalidItemName.json"));
    }

    @Test
    public void readAddressBook_invalidItemStatus_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readPreorderFixture("invalidItemStatus.json"));
    }

    @Test
    public void readAddressBook_nullPreorder_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readPreorderFixture("nullPreorder.json"));
    }

    @Test
    public void readAddressBook_nullItem_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readPreorderFixture("nullItem.json"));
    }

    @Test
    public void readAddressBook_invalidFulfilmentDate_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> readPreorderFixture("invalidFulfilmentDate.json"));
    }

    private void readPreorderFixture(String fileName) throws Exception {
        new JsonAddressBookStorage(TEST_DATA_FOLDER.resolve(fileName)).readAddressBook();
    }

    @Test
    public void readAndSaveAddressBook_fulfilmentDetails_preservesAllDetails() throws Exception {
        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(TEST_DATA_FOLDER.resolve("fulfilmentDetails.json"))
                .readAddressBook().get();

        AddressBook expected = new AddressBook();
        expected.addPerson(new PersonBuilder(ALICE).withPreorder(new PreorderBuilder()
                .withItems(new ItemBuilder().build()).withDate("2026-12-20")
                .withMethod(FulfilmentMethod.DELIVERY).withHandoverNotes("Call on arrival").build()).build());
        expected.addPerson(new PersonBuilder(BENSON).withPreorder(new PreorderBuilder()
                .withDate("2026-12-23").build()).build());
        assertEquals(expected, new AddressBook(loaded));

        Path savedPath = testFolder.resolve("fulfilmentDetailsSaved.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(savedPath);
        storage.saveAddressBook(loaded);
        assertEquals(expected, new AddressBook(storage.readAddressBook().get()));
    }

    @Test
    public void readAndSaveAddressBook_legacyPreorder_savedAsOneItemWithoutQuantity() throws Exception {
        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(TEST_DATA_FOLDER.resolve("legacyPreorder.json"))
                .readAddressBook().get();

        Item convertedItem = new ItemBuilder().withName("2 chocolate cakes, no nuts").withoutQuantity()
                .withStatus(ItemStatus.PENDING).build();
        AddressBook expected = new AddressBook();
        expected.addPerson(new PersonBuilder(ALICE).withPreorder(convertedItem).build());
        assertEquals(expected, new AddressBook(loaded));

        Path savedPath = testFolder.resolve("legacyPreorderSaved.json");
        new JsonAddressBookStorage(savedPath).saveAddressBook(loaded);
        JsonNode personJson = JsonUtil.readJsonFile(savedPath, JsonNode.class).get().get("persons").get(0);
        assertTrue(personJson.get("preorder") == null);
        assertTrue(personJson.get("preorders").get(0).get("items").get(0).get("quantity").isNull());
        assertEquals(new AddressBook(loaded), new AddressBook(new JsonAddressBookStorage(savedPath)
                .readAddressBook().get()));
    }

    @Test
    public void readAndSaveAddressBook_namesWithExtraSpaces_success() throws Exception {
        Path sourcePath = NAME_TEST_DATA_FOLDER.resolve("namesWithExtraSpacesAddressBook.json");
        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(sourcePath).readAddressBook().get();
        assertEquals(List.of("mArY-JaNe O'BrIeN", "Jose Tan"), loaded.getPersonList().stream()
                .map(person -> person.getName().fullName).toList());

        Path savedPath = testFolder.resolve("NormalizedNamesAddressBook.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(savedPath);
        storage.saveAddressBook(loaded);

        JsonNode savedJson = JsonUtil.readJsonFile(savedPath, JsonNode.class).get();
        assertEquals("mArY-JaNe O'BrIeN", savedJson.get("persons").get(0).get("name").asText());
        assertEquals("Jose Tan", savedJson.get("persons").get(1).get("name").asText());
        assertEquals(loaded, storage.readAddressBook().get());
    }

    @Test
    public void readAndSaveAddressBook_customerFieldLengthBoundaries_success() throws Exception {
        List<JsonAdaptedPerson> persons = new ArrayList<>();
        AddressBook expected = new AddressBook();
        String emoji = "\uD83C\uDF70";
        for (int offset : new int[]{-1, 0}) {
            String normalizedName = "A".repeat(98 + offset) + " B";
            String rawName = "  " + "A".repeat(98 + offset) + "   B  ";
            String email = createEmailWithLength(254 + offset);
            String address = emoji.repeat(149) + " ".repeat(2 + offset);
            String tag = "T".repeat(30 + offset);
            persons.add(new JsonAdaptedPerson(rawName, "81234567", email, address, "012345",
                    List.of(new JsonAdaptedTag(tag))));
            expected.addPerson(new PersonBuilder().withName(normalizedName).withPhone("81234567").withEmail(email)
                    .withAddress(address).withPostalCode("012345").withTags(tag).build());
        }
        Path sourcePath = testFolder.resolve("customer-boundaries.json");
        JsonUtil.saveJsonFile(new JsonSerializableAddressBook(persons), sourcePath);
        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(sourcePath).readAddressBook().get();
        assertEquals(expected, loaded);

        Path savedPath = testFolder.resolve("customer-boundaries-saved.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(savedPath);
        storage.saveAddressBook(loaded);
        assertEquals(expected, storage.readAddressBook().get());
    }

    @Test
    public void readAddressBook_overLimitCustomerFields_throwsDataLoadingException() throws Exception {
        List<JsonAdaptedTag> validTags = List.of(new JsonAdaptedTag("friends"));
        List<JsonAdaptedPerson> invalidPersons = List.of(
                new JsonAdaptedPerson("  " + "A".repeat(99) + "   B  ", "81234567", "test@example.com",
                        "Baker Road", "012345", validTags),
                new JsonAdaptedPerson("New Customer", "81234567", createEmailWithLength(255),
                        "Baker Road", "012345", validTags),
                new JsonAdaptedPerson("New Customer", "81234567", "test@example.com",
                        "A".repeat(301), "012345", validTags),
                new JsonAdaptedPerson("New Customer", "81234567", "test@example.com",
                        "\uD83C\uDF70".repeat(150) + "A", "012345", validTags),
                new JsonAdaptedPerson("New Customer", "81234567", "test@example.com",
                        "A".repeat(298) + "   ", "012345", validTags),
                new JsonAdaptedPerson("New Customer", "81234567", "test@example.com",
                        "Baker Road", "012345", List.of(new JsonAdaptedTag("friends"),
                                new JsonAdaptedTag("T".repeat(31)))));
        Path filePath = testFolder.resolve("over-limit-customer.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        for (JsonAdaptedPerson invalidPerson : invalidPersons) {
            JsonSerializableAddressBook data = new JsonSerializableAddressBook(
                    List.of(new JsonAdaptedPerson(ALICE), invalidPerson, new JsonAdaptedPerson(BENSON)));
            JsonUtil.saveJsonFile(data, filePath);
            String originalJson = Files.readString(filePath);

            assertThrows(DataLoadingException.class, storage::readAddressBook);
            assertEquals(originalJson, Files.readString(filePath));
        }
    }

    @Test
    public void readAndSaveAddressBook_sampleData_success() throws Exception {
        Path filePath = testFolder.resolve("sample-data.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        assertTrue(storage.readAddressBook().isEmpty());
        ReadOnlyAddressBook sampleData = SampleDataUtil.getSampleAddressBook();
        assertFalse(sampleData.getPersonList().isEmpty());

        storage.saveAddressBook(sampleData);

        assertEquals(sampleData, storage.readAddressBook().get());
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        Path filePath = testFolder.resolve("TempAddressBook.json");
        AddressBook original = getTypicalAddressBook();
        JsonAddressBookStorage jsonAddressBookStorage = new JsonAddressBookStorage(filePath);

        // Save in new file and read back
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        ReadOnlyAddressBook readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Modify data, overwrite existing file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        jsonAddressBookStorage.saveAddressBook(original, filePath);
        readBack = jsonAddressBookStorage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));

        // Save and read without specifying file path
        original.addPerson(IDA);
        jsonAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = jsonAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void readAndSaveAddressBook_currentPostalCodeData_preservesAllFields() throws Exception {
        Path filePath = testFolder.resolve("CurrentAddressBook.json");
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder().withName("Current Customer")
                .withPhone("81234567").withEmail("current@example.com")
                .withAddress("123 Example Street").withPostalCode("012345")
                .withTags("friends", "owesMoney")
                .withPreorder(new ItemBuilder().withName("Chocolate cake").build()).build());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original, filePath);

        ReadOnlyAddressBook readBack = storage.readAddressBook(filePath).get();
        assertEquals(original, new AddressBook(readBack));
        assertEquals("012345", readBack.getPersonList().get(0).getPostalCode().value);
        assertEquals(2, readBack.getPersonList().get(0).getTags().size());
    }

    @Test
    public void readAndSaveAddressBook_preorder_success() throws Exception {
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder(ALICE).withPreorder(
                new ItemBuilder().withName("Pineapple Tarts").withRemark("Less sugar").build(),
                new ItemBuilder().withName("Almond Cookies").withQuantity("1")
                        .withStatus(ItemStatus.COMPLETED).build()).build());

        Path filePath = testFolder.resolve("nested").resolve("preorders.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);

        ReadOnlyAddressBook readBack = storage.readAddressBook().get();
        assertEquals(original, new AddressBook(readBack));
        JsonNode savedJson = JsonUtil.readJsonFile(filePath, JsonNode.class).get();
        JsonNode preorders = savedJson.get("persons").get(0).get("preorders");
        assertEquals(1, preorders.size());
        JsonNode items = preorders.get(0).get("items");
        assertEquals("Pineapple Tarts", items.get(0).get("name").asText());
        assertEquals("2", items.get(0).get("quantity").asText());
        assertEquals("PENDING", items.get(0).get("status").asText());
        assertEquals("Less sugar", items.get(0).get("remark").asText());
        assertEquals("Almond Cookies", items.get(1).get("name").asText());
        assertEquals("COMPLETED", items.get(1).get("status").asText());
    }

    @Test
    public void readAndSaveAddressBook_updateAndDeletePreorder_success() throws Exception {
        Path filePath = testFolder.resolve("preorder-lifecycle.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        AddressBook withPendingPreorder = new AddressBook();
        withPendingPreorder.addPerson(new PersonBuilder(ALICE)
                .withPreorder("2 chocolate cakes", ItemStatus.PENDING).build());
        storage.saveAddressBook(withPendingPreorder);

        AddressBook withCompletedPreorder = new AddressBook();
        withCompletedPreorder.addPerson(new PersonBuilder(ALICE)
                .withPreorder("2 chocolate cakes", ItemStatus.COMPLETED).build());
        storage.saveAddressBook(withCompletedPreorder);
        assertEquals(withCompletedPreorder, new AddressBook(storage.readAddressBook().get()));

        AddressBook withoutPreorder = new AddressBook();
        withoutPreorder.addPerson(new PersonBuilder(ALICE).build());
        storage.saveAddressBook(withoutPreorder);
        assertEquals(withoutPreorder, new AddressBook(storage.readAddressBook().get()));
    }

    @Test
    public void readAndSaveAddressBook_customerWithoutPreorder_savesEmptyPreorderList() throws Exception {
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        Path filePath = testFolder.resolve("without-preorder.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original);

        JsonNode personJson = JsonUtil.readJsonFile(filePath, JsonNode.class).get()
                .get("persons").get(0);
        assertTrue(personJson.get("preorders").isArray());
        assertEquals(0, personJson.get("preorders").size());
        assertEquals(original, new AddressBook(storage.readAddressBook().get()));
    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.json"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new JsonAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }

    private static String createEmailWithLength(int length) {
        String prefix = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + ".";
        return prefix + "d".repeat(length - prefix.length());
    }
}
