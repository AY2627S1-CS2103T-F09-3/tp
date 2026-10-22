package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.DeliveryPreparationSummary;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.search.SearchCriteria;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PreorderBuilder;

/** Exercises the parser, shared criteria, model view and real JSON storage together. */
public class FulfilmentSearchIntegrationTest {
    @TempDir
    public Path temporaryFolder;

    private ModelManager model;
    private Logic logic;
    private JsonAddressBookStorage storage;
    private Person alice;
    private Person collection;
    private Person missingPostal;
    private Person emptyDelivery;
    private Person unset;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressbook.json"));
        logic = new LogicManager(model, new StorageManager(storage,
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json"))));
        alice = new PersonBuilder().withName("Alice Tan").withPostalCode("012345").withTags("regular")
                .withPreorder(new PreorderBuilder().withDate("2026-12-20").withMethod(FulfilmentMethod.DELIVERY)
                        .withHandoverNotes("Call on arrival").withItems(new ItemBuilder().build(),
                                new ItemBuilder().withStatus(ItemStatus.COMPLETED).build()).build()).build();
        collection = new PersonBuilder().withName("Bernice Yu").withPostalCode("539999")
                .withPreorder(new PreorderBuilder().withDate("2026-12-20").withMethod(FulfilmentMethod.COLLECTION)
                        .withHandoverNotes("Call at the gate").build()).build();
        missingPostal = new PersonBuilder().withName("Charlie Tan")
                .withPreorder(new PreorderBuilder().withDate("2026-12-20").withMethod(FulfilmentMethod.DELIVERY)
                        .withHandoverNotes("Call on arrival").withItems(new ItemBuilder()
                                .withStatus(ItemStatus.PREPARED).build()).build()).build();
        emptyDelivery = new PersonBuilder().withName("David Li").withPostalCode("530123")
                .withPreorder(new PreorderBuilder().withDate("2026-12-21")
                        .withMethod(FulfilmentMethod.DELIVERY).build()).build();
        unset = new PersonBuilder().withName("Unset Details").withPreorder(new ItemBuilder().build()).build();
        List.of(alice, collection, missingPostal, emptyDelivery, unset,
                new PersonBuilder().withName("No Preorder").build()).forEach(model::addPerson);
    }

    @Test
    public void search_matchesSharedFieldsIncludingEmptyPreorders() throws Exception {
        find("find m/delivery", List.of(alice, missingPostal, emptyDelivery));
        find("find m/COLLECTION", List.of(collection));
        find("find f/2026-12-20", List.of(alice, collection, missingPostal));
        find("find h/GATE", List.of(collection));
        find("find f/2026-12-20 m/delivery h/ARRIVAL n/Alice t/regular", List.of(alice));
    }

    @Test
    public void handoverNotesSearch_ignoresOtherFieldsAndCombinesUsingAnd() throws Exception {
        Person differentNotes = new PersonBuilder().withName("Arrival Customer").withAddress("12 Arrival Lane")
                .withPreorder(new PreorderBuilder().withMethod(FulfilmentMethod.DELIVERY)
                        .withHandoverNotes("Use the side gate").withItems(new ItemBuilder()
                                .withRemark("Call on arrival").build()).build()).build();
        model.addPerson(differentNotes);

        find("find h/ARRIVAL", List.of(alice, missingPostal));
        find("find h/arrival n/Alice m/delivery f/2026-12-20 t/regular", List.of(alice));
        find("find h/arrival m/collection", List.of());
        find("find h/GATE", List.of(collection, differentNotes));
    }

    @Test
    public void successiveFindAndTrack_replacePreviousCriteria() throws Exception {
        find("find m/collection", List.of(collection));
        find("find h/arrival", List.of(alice, missingPostal));
        find("find n/Alice", List.of(alice));
        logic.execute("track");
        assertEquals(List.of(alice, missingPostal, unset), logic.getFilteredPersonList());
        find("find m/collection", List.of(collection));
        logic.execute("list");
        assertEquals(6, logic.getFilteredPersonList().size());
    }

    @Test
    public void invalidSearch_preservesCriteriaResultsAndCustomerData() throws Exception {
        find("find m/delivery", List.of(alice, missingPostal, emptyDelivery));
        SearchCriteria criteria = logic.getActiveSearchCriteria();
        List<Person> customers = List.copyOf(model.getAddressBook().getPersonList());
        for (String input : List.of("find f/2026-02-30", "find m/pickup", "find h/", "find f/",
                "find m/delivery m/collection", "find n/Alice h/", "find i/arrival", "find h/arrival i/1",
                "find Alice Charlie", "find Alice m/delivery")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(criteria, logic.getActiveSearchCriteria());
            assertEquals(List.of(alice, missingPostal, emptyDelivery), logic.getFilteredPersonList());
            assertEquals(customers, model.getAddressBook().getPersonList());
        }
    }

    @Test
    public void postalFulfilmentAndItemSearch_requiresEveryConditionAndRefreshesSummary() throws Exception {
        PreorderBuilder preorder = new PreorderBuilder(alice.getPreorder().orElseThrow());
        Person wrongCode = new PersonBuilder(alice).withName("Wrong Code").withPostalCode("530123").build();
        Person wrongDate = new PersonBuilder(alice).withName("Wrong Date")
                .withPreorder(preorder.withDate("2026-12-21").build()).build();
        Person wrongMethod = new PersonBuilder(alice).withName("Wrong Method")
                .withPreorder(preorder.withDate("2026-12-20").withMethod(FulfilmentMethod.COLLECTION).build()).build();
        Person wrongNotes = new PersonBuilder(alice).withName("Wrong Notes")
                .withPreorder(preorder.withMethod(FulfilmentMethod.DELIVERY)
                        .withHandoverNotes("Use the gate").build()).build();
        Person splitMatch = new PersonBuilder(alice).withName("Split Match")
                .withPreorder(preorder.withHandoverNotes("Call on arrival").withItems(
                        new ItemBuilder().withStatus(ItemStatus.COMPLETED).build(),
                        new ItemBuilder().withName("Cookies").build()).build()).build();
        List.of(wrongCode, wrongDate, wrongMethod, wrongNotes, splitMatch).forEach(model::addPerson);

        find("find c/01 f/2026-12-20 m/delivery h/arrival d/Pineapple Tarts s/pending", List.of(alice));
        SearchCriteria criteria = logic.getActiveSearchCriteria();
        logic.execute("group postal");
        DeliveryPreparationSummary summary = new DeliveryPreparationSummary(logic.getFilteredPersonList());
        assertEquals(List.of(alice), summary.getStops());
        assertEquals(1, summary.getPostalGroupCount());

        logic.execute("edititem 1 i/1 s/completed");
        assertEquals(criteria, logic.getActiveSearchCriteria());
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertEquals(0, new DeliveryPreparationSummary(logic.getFilteredPersonList()).getStopCount());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        Person updatedAlice = model.getAddressBook().getPersonList().getFirst();
        find("find c/012345 f/2026-12-20 m/delivery h/arrival d/Pineapple Tarts s/completed",
                List.of(updatedAlice, splitMatch));
        assertEquals(List.of(splitMatch), new DeliveryPreparationSummary(logic.getFilteredPersonList()).getStops());
    }

    @Test
    public void groupedFulfilmentSearch_editsPreserveAllFieldsAndStorage() throws Exception {
        find("find m/delivery f/2026-12-20", List.of(alice, missingPostal));
        SearchCriteria criteria = logic.getActiveSearchCriteria();
        logic.execute("group postal");
        assertEquals(criteria, logic.getActiveSearchCriteria());
        assertEquals(List.of(alice, missingPostal), logic.getFilteredPersonList());
        logic.execute("edit 1 p/89999999");
        logic.execute("tag 1 t/vip");
        logic.execute("untag 1 t/VIP");
        Person edited = logic.getFilteredPersonList().getFirst();
        assertEquals("012345", edited.getPostalCode().value);
        assertEquals(alice.getPreorder(), edited.getPreorder());
        assertEquals(criteria, logic.getActiveSearchCriteria());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        DeliveryPreparationSummary summary = new DeliveryPreparationSummary(logic.getFilteredPersonList());
        assertEquals(2, summary.getStopCount());
        assertEquals(2, summary.getPostalGroupCount());
        logic.execute("deleteorder 1");
        assertEquals(List.of(missingPostal), logic.getFilteredPersonList());
        assertEquals(1, new DeliveryPreparationSummary(logic.getFilteredPersonList()).getStopCount());
    }

    @Test
    public void preorderDateUpdate_preservesOtherFieldsAndRefreshesFulfilmentSearch() throws Exception {
        find("find c/01 f/2026-12-20 m/delivery h/arrival d/Pineapple Tarts s/pending", List.of(alice));
        SearchCriteria criteria = logic.getActiveSearchCriteria();
        Preorder expectedPreorder = new PreorderBuilder(alice.getPreorder().orElseThrow())
                .withDate("2026-12-21").build();
        Person updatedAlice = new PersonBuilder(alice).withPreorder(expectedPreorder).build();
        List<Person> expectedCustomers = new ArrayList<>(model.getAddressBook().getPersonList());
        expectedCustomers.set(0, updatedAlice);

        logic.execute("preorder 1 f/2026-12-21");

        assertEquals(criteria, logic.getActiveSearchCriteria());
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertEquals(expectedCustomers, model.getAddressBook().getPersonList());
        assertEquals(0, new DeliveryPreparationSummary(logic.getFilteredPersonList()).getStopCount());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());

        find("find c/01 f/2026-12-21 m/delivery h/arrival d/Pineapple Tarts s/pending", List.of(updatedAlice));
        assertEquals(List.of(updatedAlice), new DeliveryPreparationSummary(logic.getFilteredPersonList()).getStops());
    }

    @Test
    public void preorderClearAndSet_refreshDeliveryStopsWithoutLosingItemsOrCustomerDetails() throws Exception {
        find("find c/012345 f/2026-12-20 m/delivery h/arrival", List.of(alice));
        logic.execute("group postal");
        SearchCriteria criteria = logic.getActiveSearchCriteria();
        Preorder clearedPreorder = new Preorder(alice.getPreorder().orElseThrow().getItems());
        Person clearedAlice = new PersonBuilder(alice).withPreorder(clearedPreorder).build();
        List<Person> expectedCustomers = new ArrayList<>(model.getAddressBook().getPersonList());
        expectedCustomers.set(0, clearedAlice);

        logic.execute("preorder 1 f/ m/ h/");

        assertEquals(criteria, logic.getActiveSearchCriteria());
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertEquals(expectedCustomers, model.getAddressBook().getPersonList());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        find("find c/012345", List.of(clearedAlice));
        assertEquals(0, new DeliveryPreparationSummary(logic.getFilteredPersonList()).getStopCount());

        criteria = logic.getActiveSearchCriteria();
        logic.execute("preorder 1 m/delivery");
        Person deliveryAlice = new PersonBuilder(alice).withPreorder(new PreorderBuilder(clearedPreorder)
                .withMethod(FulfilmentMethod.DELIVERY).build()).build();
        expectedCustomers.set(0, deliveryAlice);
        assertEquals(criteria, logic.getActiveSearchCriteria());
        assertEquals(List.of(deliveryAlice), logic.getFilteredPersonList());
        assertEquals(expectedCustomers, model.getAddressBook().getPersonList());
        assertEquals(List.of(deliveryAlice), new DeliveryPreparationSummary(logic.getFilteredPersonList()).getStops());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private void find(String command, List<Person> expected) throws Exception {
        logic.execute(command);
        assertEquals(expected, logic.getFilteredPersonList());
    }
}
