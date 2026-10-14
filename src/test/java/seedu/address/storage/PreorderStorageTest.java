package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.PreorderStatus;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class PreorderStorageTest {
    @TempDir
    public Path testFolder;

    @Test
    public void saveAndRead_allStatuses_preservesCustomersAndPreorders() throws Exception {
        Path path = testFolder.resolve("orders.json");
        for (PreorderStatus status : PreorderStatus.values()) {
            Person orderedAlice = new PersonBuilder(ALICE).withPreorder("Cake", status).build();
            AddressBook expected = new AddressBookBuilder().withPerson(orderedAlice).withPerson(BENSON).build();
            new JsonAddressBookStorage(path).saveAddressBook(expected);
            assertEquals(expected, new JsonAddressBookStorage(path).readAddressBook().orElseThrow());
            JsonNode order = JsonUtil.readJsonFile(path, JsonNode.class).orElseThrow()
                    .get("persons").get(0).get("preorder");
            assertEquals("Cake", order.get("preorderDetails").asText());
            assertEquals(status.name(), order.get("preorderStatus").asText());
        }
    }

    @Test
    public void read_legacyMissingPreorderOrExplicitNull_loadsWithoutPreorder() throws Exception {
        for (boolean removeField : new boolean[]{true, false}) {
            ObjectNode person = personJson();
            if (removeField) {
                person.remove("preorder");
            } else {
                person.putNull("preorder");
            }
            Path path = writePerson(person);
            Person restored = new JsonAddressBookStorage(path).readAddressBook().orElseThrow().getPersonList().get(0);
            assertEquals(ALICE, restored);
            assertTrue(restored.getPreorder().isEmpty());
        }
    }

    @Test
    public void read_malformedPreorder_rejectsFile() throws Exception {
        for (String order : new String[]{"{}", "{\"preorderDetails\":\"Cake\"}",
            "{\"preorderStatus\":\"PENDING\"}",
            "{\"preorderDetails\":\"\",\"preorderStatus\":\"PENDING\"}",
            "{\"preorderDetails\":\"Cake\",\"preorderStatus\":\"DONE\"}"}) {
            ObjectNode person = personJson();
            person.set("preorder", JsonUtil.fromJsonString(order, JsonNode.class));
            JsonAddressBookStorage storage = new JsonAddressBookStorage(writePerson(person));
            assertThrows(DataLoadingException.class, storage::readAddressBook);
        }
    }

    @Test
    public void orderCommand_saveReloadAndPartialUpdate_preservesLatestOrder() throws Exception {
        Path path = testFolder.resolve("command-orders.json");
        Model model = new ModelManager(new AddressBookBuilder().withPerson(ALICE).build(), new UserPrefs());
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("order 1 d/Cake s/pending").execute(model);
        new JsonAddressBookStorage(path).saveAddressBook(model.getAddressBook());

        Model restored = new ModelManager(new JsonAddressBookStorage(path).readAddressBook().orElseThrow(),
                new UserPrefs());
        parser.parseCommand("order 1 s/completed").execute(restored);
        new JsonAddressBookStorage(path).saveAddressBook(restored.getAddressBook());

        Person expected = new PersonBuilder(ALICE).withPreorder("Cake", PreorderStatus.COMPLETED).build();
        assertEquals(expected, new JsonAddressBookStorage(path).readAddressBook().orElseThrow().getPersonList().get(0));
    }

    private ObjectNode personJson() throws Exception {
        return JsonUtil.fromJsonString(JsonUtil.toJsonString(new JsonAdaptedPerson(ALICE)), ObjectNode.class);
    }

    private Path writePerson(ObjectNode person) throws Exception {
        Path path = testFolder.resolve("input.json");
        Files.writeString(path, "{\"persons\":[" + person + "]}");
        return path;
    }
}
