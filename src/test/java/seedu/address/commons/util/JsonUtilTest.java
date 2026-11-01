package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @TempDir
    public Path testFolder;

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    @Test
    public void readJsonFile_validFile_loadsWithoutChangingFile() throws Exception {
        Path filePath = testFolder.resolve("valid.json");
        Files.writeString(filePath, SerializableTestClass.JSON_STRING_REPRESENTATION);
        byte[] originalContent = Files.readAllBytes(filePath);

        SerializableTestClass loaded = JsonUtil.readJsonFile(filePath, SerializableTestClass.class).get();

        assertEquals(SerializableTestClass.getNameTestValue(), loaded.getName());
        assertEquals(SerializableTestClass.getListTestValues(), loaded.getListOfLocalDateTimes());
        assertEquals(SerializableTestClass.getHashMapTestValues(), loaded.getMapOfIntegerToString());
        assertArrayEquals(originalContent, Files.readAllBytes(filePath));
    }

    @Test
    public void readJsonFile_missingFile_returnsEmptyWithoutCreatingFile() throws Exception {
        Path filePath = testFolder.resolve("missing.json");

        assertTrue(JsonUtil.readJsonFile(filePath, SerializableTestClass.class).isEmpty());
        assertFalse(Files.exists(filePath));
    }

    @Test
    public void readJsonFile_missingParent_returnsEmptyWithoutCreatingDirectory() throws Exception {
        Path filePath = testFolder.resolve("missing").resolve("data.json");

        assertTrue(JsonUtil.readJsonFile(filePath, SerializableTestClass.class).isEmpty());
        assertFalse(Files.exists(filePath.getParent()));
    }

    @Test
    public void readJsonFile_invalidJson_throwsWithoutChangingFile() throws Exception {
        assertReadFailurePreservesFile("{invalid json");
    }

    @Test
    public void readJsonFile_nullJson_throwsWithoutChangingFile() throws Exception {
        assertReadFailurePreservesFile("null");
    }

    @Test
    public void readJsonFile_emptyFile_throwsWithoutChangingFile() throws Exception {
        assertReadFailurePreservesFile("");
    }

    @Test
    public void readJsonFile_directory_throwsDataLoadingException() {
        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(testFolder, SerializableTestClass.class));
        assertTrue(Files.isDirectory(testFolder));
    }

    private void assertReadFailurePreservesFile(String contents) throws Exception {
        Path filePath = testFolder.resolve("invalid.json");
        Files.writeString(filePath, contents);
        byte[] originalContent = Files.readAllBytes(filePath);

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(filePath, SerializableTestClass.class));
        assertArrayEquals(originalContent, Files.readAllBytes(filePath));
    }

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
