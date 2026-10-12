package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.preorder.PreorderStatus;
import seedu.address.testutil.PersonBuilder;

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
    public void readAndSaveAddressBook_preorder_success() throws Exception {
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder(ALICE)
                .withPreorder("2 chocolate cakes", PreorderStatus.COMPLETED).build());

        Path filePath = testFolder.resolve("nested").resolve("preorders.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);
        storage.saveAddressBook(original);

        ReadOnlyAddressBook readBack = storage.readAddressBook().get();
        assertEquals(original, new AddressBook(readBack));
        JsonNode savedJson = JsonUtil.readJsonFile(filePath, JsonNode.class).get();
        assertEquals("2 chocolate cakes", savedJson.get("persons").get(0)
                .get("preorder").get("details").asText());
        assertEquals("COMPLETED", savedJson.get("persons").get(0)
                .get("preorder").get("status").asText());
    }

    @Test
    public void readAndSaveAddressBook_updateAndDeletePreorder_success() throws Exception {
        Path filePath = testFolder.resolve("preorder-lifecycle.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        AddressBook withPendingPreorder = new AddressBook();
        withPendingPreorder.addPerson(new PersonBuilder(ALICE)
                .withPreorder("2 chocolate cakes", PreorderStatus.PENDING).build());
        storage.saveAddressBook(withPendingPreorder);

        AddressBook withCompletedPreorder = new AddressBook();
        withCompletedPreorder.addPerson(new PersonBuilder(ALICE)
                .withPreorder("2 chocolate cakes", PreorderStatus.COMPLETED).build());
        storage.saveAddressBook(withCompletedPreorder);
        assertEquals(withCompletedPreorder, new AddressBook(storage.readAddressBook().get()));

        AddressBook withoutPreorder = new AddressBook();
        withoutPreorder.addPerson(new PersonBuilder(ALICE).build());
        storage.saveAddressBook(withoutPreorder);
        assertEquals(withoutPreorder, new AddressBook(storage.readAddressBook().get()));
    }

    @Test
    public void readAndSaveAddressBook_customerWithoutPreorder_preservesLegacyShape() throws Exception {
        AddressBook original = new AddressBook();
        original.addPerson(ALICE);
        Path filePath = testFolder.resolve("without-preorder.json");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(filePath);

        storage.saveAddressBook(original);

        JsonNode personJson = JsonUtil.readJsonFile(filePath, JsonNode.class).get()
                .get("persons").get(0);
        assertTrue(personJson.get("preorder") == null || personJson.get("preorder").isNull());
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
}
