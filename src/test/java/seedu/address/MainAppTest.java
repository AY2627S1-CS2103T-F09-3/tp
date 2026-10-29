package seedu.address;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.person.Email;
import seedu.address.model.person.Phone;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;

public class MainAppTest {

    private static final Path STORAGE_TEST_DATA_FOLDER = Path.of("src", "test", "data", "JsonAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @BeforeAll
    public static void startJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another UI test has already started JavaFX.
        }
        // Dismissing the startup alert must not stop the toolkit used by other UI tests.
        Platform.setImplicitExit(false);
    }

    @Test
    public void init_validFile_loadsSavedDataWithoutChangingFile() throws Exception {
        Path dataPath = dataPath();
        AddressBook expected = getTypicalAddressBook();
        new JsonAddressBookStorage(dataPath).saveAddressBook(expected);
        byte[] originalContent = Files.readAllBytes(dataPath);
        MainApp app = new MainApp(prefsPath(), dataPath);

        app.init();

        assertEquals(expected, app.model.getAddressBook());
        assertNotNull(app.logic);
        assertNotNull(app.ui);
        assertArrayEquals(originalContent, Files.readAllBytes(dataPath));
    }

    @Test
    public void init_missingFile_usesSampleDataAndSavesAfterCommand() throws Exception {
        MainApp app = new MainApp(prefsPath(), dataPath());

        app.init();

        assertEquals(SampleDataUtil.getSampleAddressBook(), app.model.getAddressBook());
        assertFalse(Files.exists(dataPath()));
        app.logic.execute("list");
        assertEquals(SampleDataUtil.getSampleAddressBook(),
                new JsonAddressBookStorage(dataPath()).readAddressBook().get());
    }

    @Test
    public void init_onlyOldFileExists_usesSampleDataAndPreservesOldFile() throws Exception {
        Path oldDataPath = testFolder.resolve("data").resolve("addressbook.json");
        new JsonAddressBookStorage(oldDataPath).saveAddressBook(new AddressBook());
        byte[] originalContent = Files.readAllBytes(oldDataPath);
        MainApp app = new MainApp(prefsPath(), dataPath());

        app.init();
        app.logic.execute("list");

        assertEquals(SampleDataUtil.getSampleAddressBook(),
                new JsonAddressBookStorage(dataPath()).readAddressBook().get());
        assertArrayEquals(originalContent, Files.readAllBytes(oldDataPath));
    }

    @Test
    public void init_invalidJson_skipsNormalInitializationAndPreservesFile() throws Exception {
        assertFailedInitializationPreservesFile("{invalid json");
    }

    @Test
    public void init_invalidRecord_skipsNormalInitializationAndPreservesFile() throws Exception {
        assertFailedInitializationPreservesFile("{\"persons\":[{}]}");
    }

    @Test
    public void init_readFailure_skipsNormalInitializationAndPreservesDirectory() throws Exception {
        Files.createDirectories(dataPath());
        Path marker = dataPath().resolve("keep.txt");
        Files.writeString(marker, "Keep this file");
        MainApp app = new MainApp(prefsPath(), dataPath());

        assertDoesNotThrow(app::init);

        assertNormalInitializationSkipped(app);
        assertDoesNotThrow(app::stop);
        assertTrue(Files.isDirectory(dataPath()));
        assertEquals("Keep this file", Files.readString(marker));
    }

    @Test
    public void start_loadFailure_showsErrorBeforeExitAndPreservesFile() throws Exception {
        Files.createDirectories(dataPath().getParent());
        Files.writeString(dataPath(), "{invalid json");
        byte[] originalContent = Files.readAllBytes(dataPath());
        TestMainApp app = new TestMainApp(prefsPath(), dataPath());
        app.init();
        app.ui = stage -> fail("The main UI must not start after a loading failure.");
        byte[] originalPrefs = Files.readAllBytes(prefsPath());

        runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                app.start(stage);

                Alert alert = app.startupAlert;
                assertEquals(Alert.AlertType.ERROR, alert.getAlertType());
                assertEquals("LeBake could not start", alert.getTitle());
                assertEquals("Unable to load saved data", alert.getHeaderText());
                assertTrue(alert.getContentText().contains(dataPath().toAbsolutePath().normalize().toString()));
                assertTrue(alert.getContentText().contains("Reason:\nInvalid JSON:"));
                assertTrue(alert.getContentText().contains("Unexpected character"));
                assertTrue(alert.getContentText().contains("Line 1, column "));
                assertFalse(alert.getContentText().contains("[Source:"));
                assertTrue(alert.getContentText().contains("Your data file has not been changed."));
                assertTrue(alert.getContentText().contains("permission to read"));
                assertTrue(alert.getContentText().contains("restore a valid backup"));
                assertNull(alert.getOwner());
                assertEquals(ButtonType.OK, alert.getResult());
                assertFalse(stage.isShowing());
                assertTrue(app.hasExited);
            } finally {
                stage.close();
            }
        });

        app.stop();
        assertArrayEquals(originalContent, Files.readAllBytes(dataPath()));
        assertArrayEquals(originalPrefs, Files.readAllBytes(prefsPath()));
    }

    @Test
    public void createDataLoadingErrorDialog_invalidPhone_identifiesPersonAndField() throws Exception {
        String data = Files.readString(STORAGE_TEST_DATA_FOLDER.resolve("fulfilmentDetails.json"))
                .replace("\"94351253\"", "\"123\"");

        String content = getErrorDialogContent(data);

        assertTrue(content.contains("Reason:\nPerson 1: invalid phone."));
        assertFalse(content.contains(Phone.MESSAGE_CONSTRAINTS));
        assertFalse(content.contains("IllegalValueException"));
    }

    @Test
    public void createDataLoadingErrorDialog_invalidQuantity_identifiesPersonItemAndField() throws Exception {
        String data = Files.readString(STORAGE_TEST_DATA_FOLDER.resolve("fulfilmentDetails.json"))
                .replace("\"quantity\" : \"2\"", "\"quantity\" : \"0\"");

        String content = getErrorDialogContent(data);

        assertTrue(content.contains("Reason:\nPerson 1: item 1: invalid quantity."));
        assertFalse(content.contains(Quantity.MESSAGE_CONSTRAINTS));
    }

    @Test
    public void createDataLoadingErrorDialog_missingItemStatus_identifiesMissingField() throws Exception {
        String data = Files.readString(STORAGE_TEST_DATA_FOLDER.resolve("missingItemStatus.json"));

        String content = getErrorDialogContent(data);

        assertTrue(content.contains("Reason:\nPerson 1: item 1: missing status."));
    }

    @Test
    public void createDataLoadingErrorDialog_nullDocument_showsOriginalReadError() throws Exception {
        String content = getErrorDialogContent("null");

        assertTrue(content.contains("Reason:\nJSON file must not contain null."));
    }

    @Test
    public void createDataLoadingErrorDialog_invalidSecondEmail_identifiesPersonAndField() throws Exception {
        String data = Files.readString(STORAGE_TEST_DATA_FOLDER.resolve("fulfilmentDetails.json"))
                .replace("\"johnd@example.com\"", "\"not-an-email\"");

        String content = getErrorDialogContent(data);

        assertTrue(content.contains("Reason:\nPerson 2: invalid email."));
        assertFalse(content.contains(Email.MESSAGE_CONSTRAINTS));
    }

    @Test
    public void createDataLoadingErrorDialog_emptyEmail_identifiesPersonAndField() throws Exception {
        String data = Files.readString(STORAGE_TEST_DATA_FOLDER.resolve("fulfilmentDetails.json"))
                .replace("\"alice@example.com\"", "\"\"");

        String content = getErrorDialogContent(data);

        assertTrue(content.contains("Reason:\nPerson 1: invalid email."));
        assertFalse(content.contains(Email.MESSAGE_CONSTRAINTS));
    }

    @Test
    public void createDataLoadingErrorDialog_missingSecondEmail_identifiesPersonAndField() throws Exception {
        String data = Files.readString(STORAGE_TEST_DATA_FOLDER.resolve("fulfilmentDetails.json"))
                .replace("\"email\" : \"johnd@example.com\",", "");

        String content = getErrorDialogContent(data);

        assertTrue(content.contains("Reason:\nPerson 2: missing email."));
    }

    @Test
    public void createDataLoadingErrorDialog_invalidSecondItem_identifiesPersonItemAndField() throws Exception {
        JsonNode data = JsonUtil.readJsonFile(STORAGE_TEST_DATA_FOLDER.resolve("fulfilmentDetails.json"),
                JsonNode.class).get();
        ArrayNode items = (ArrayNode) data.get("persons").get(0).get("preorders").get(0).get("items");
        ObjectNode invalidItem = ((ObjectNode) items.get(0)).deepCopy();
        invalidItem.put("quantity", "0");
        items.add(invalidItem);

        String content = getErrorDialogContent(JsonUtil.toJsonString(data));

        assertTrue(content.contains("Reason:\nPerson 1: item 2: invalid quantity."));
    }

    @Test
    public void start_validData_startsNormalUi() throws Exception {
        MainApp app = new MainApp(prefsPath(), dataPath());
        app.init();
        AtomicReference<Stage> startedStage = new AtomicReference<>();
        app.ui = startedStage::set;

        runOnFxThread(() -> {
            Stage stage = new Stage();
            try {
                app.start(stage);

                assertSame(stage, startedStage.get());
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void stop_beforeInitialization_doesNotThrow() {
        assertDoesNotThrow(new MainApp()::stop);
    }

    @Test
    public void stop_successfulInitialization_savesPreferences() throws Exception {
        MainApp app = new MainApp(prefsPath(), dataPath());
        app.init();
        GuiSettings expected = new GuiSettings(900, 700, 10, 20);
        app.model.setGuiSettings(expected);

        app.stop();

        assertEquals(expected, new JsonUserPrefsStorage(prefsPath()).readUserPrefs().get().getGuiSettings());
    }

    private void assertFailedInitializationPreservesFile(String contents) throws Exception {
        Files.createDirectories(dataPath().getParent());
        Files.writeString(dataPath(), contents);
        byte[] originalContent = Files.readAllBytes(dataPath());
        MainApp app = new MainApp(prefsPath(), dataPath());

        assertDoesNotThrow(app::init);

        assertNormalInitializationSkipped(app);
        assertDoesNotThrow(app::stop);
        assertArrayEquals(originalContent, Files.readAllBytes(dataPath()));
    }

    private String getErrorDialogContent(String contents) throws Exception {
        Files.createDirectories(dataPath().getParent());
        Files.writeString(dataPath(), contents);
        byte[] originalContent = Files.readAllBytes(dataPath());
        MainApp app = new MainApp(prefsPath(), dataPath());
        app.init();
        assertNormalInitializationSkipped(app);
        AtomicReference<String> content = new AtomicReference<>();

        runOnFxThread(() -> content.set(app.createDataLoadingErrorDialog().getContentText()));

        app.stop();
        assertArrayEquals(originalContent, Files.readAllBytes(dataPath()));
        return content.get();
    }

    private static void assertNormalInitializationSkipped(MainApp app) {
        assertNotNull(app.storage);
        assertNull(app.model);
        assertNull(app.logic);
        assertNull(app.ui);
    }

    private Path dataPath() {
        return testFolder.resolve("data").resolve("lebake.json");
    }

    private Path prefsPath() {
        return testFolder.resolve("preferences.json");
    }

    private static void runOnFxThread(Executable action) throws Exception {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.execute();
                result.complete(null);
            } catch (Throwable error) {
                result.completeExceptionally(error);
            }
        });
        result.get(10, TimeUnit.SECONDS);
    }

    private static class TestMainApp extends MainApp {
        private Alert startupAlert;
        private boolean hasExited;

        TestMainApp(Path prefsPath, Path dataPath) {
            super(prefsPath, dataPath);
        }

        @Override
        Alert createDataLoadingErrorDialog() {
            startupAlert = super.createDataLoadingErrorDialog();
            startupAlert.setOnShown(event -> {
                Platform.runLater(() -> ((Button) startupAlert.getDialogPane().lookupButton(ButtonType.OK)).fire());
            });
            return startupAlert;
        }

        @Override
        void exit() {
            assertFalse(startupAlert.isShowing());
            hasExited = true;
        }
    }
}
