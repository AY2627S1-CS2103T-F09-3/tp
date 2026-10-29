package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.model.preorder.ItemStatus.COMPLETED;
import static seedu.address.model.preorder.ItemStatus.PENDING;
import static seedu.address.model.preorder.ItemStatus.PREPARED;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.search.SearchCriteria;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

public class FindItemCommandIntegrationTest {
    private static final Person ALICE = new PersonBuilder().withName("Alice").withTags("regular")
            .withPreorder(new ItemBuilder().withName("Pineapple Tarts").withStatus(PENDING).build(),
                    new ItemBuilder().withName("Almond Cookies").withStatus(PREPARED).build()).build();
    private static final Person BEN = new PersonBuilder().withName("Ben")
            .withPreorder(new ItemBuilder().withName("Pineapple Tarts").withStatus(PREPARED).build()).build();
    private static final Person CARL = new PersonBuilder().withName("Carl")
            .withPreorder(new ItemBuilder().withName("Pineapple Tarts").withStatus(COMPLETED).build()).build();
    private final Model model = new ModelManager(new AddressBookBuilder()
            .withPerson(ALICE).withPerson(BEN).withPerson(CARL).build(), new UserPrefs());
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void execute_productOnly_includesEveryStatus() throws Exception {
        assertResults("find d/PINEAPPLE TARTS", ALICE, BEN, CARL);
        assertResults("find d/Pineapple");
    }

    @Test
    public void execute_combinedConditions_requireSameItemAndCustomer() throws Exception {
        assertResults("find d/Pineapple Tarts s/prepared", BEN);
        assertResults("find n/Alice d/Pineapple Tarts s/prepared");
        assertResults("find t/regular d/Pineapple Tarts s/pending", ALICE);
    }

    @Test
    public void execute_statusOnly_replacesPreviousSearch() throws Exception {
        assertResults("find n/Carl", CARL);
        assertResults("find s/prepared", ALICE, BEN);
    }

    @Test
    public void execute_invalidItemCriteria_preservesSearch() throws Exception {
        assertResults("find d/Pineapple Tarts s/pending", ALICE);
        SearchCriteria criteria = model.getActiveSearchCriteria();
        for (String command : List.of("find d/", "find s/unknown", "find s/pending s/prepared")) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command).execute(model));
            assertEquals(criteria, model.getActiveSearchCriteria());
            assertEquals(List.of(ALICE), model.getFilteredPersonList());
        }
    }

    @Test
    public void execute_findAndTrackStatus_shareCriteriaAndResults() throws Exception {
        for (String status : List.of("pending", "prepared", "completed")) {
            parser.parseCommand("find s/" + status).execute(model);
            SearchCriteria criteria = model.getActiveSearchCriteria();
            List<Person> customers = List.copyOf(model.getFilteredPersonList());

            parser.parseCommand("track s/" + status).execute(model);

            assertEquals(criteria, model.getActiveSearchCriteria());
            assertEquals(customers, model.getFilteredPersonList());
        }
    }

    @Test
    public void execute_itemChanges_refreshProductSearchWithoutResettingCriteria() throws Exception {
        assertResults("find d/Pineapple Tarts s/pending", ALICE);
        SearchCriteria criteria = model.getActiveSearchCriteria();
        Person pendingBen = new PersonBuilder(BEN)
                .withPreorder(new ItemBuilder().withName("Pineapple Tarts").withStatus(PENDING).build()).build();
        model.setPerson(BEN, pendingBen);
        assertEquals(List.of(ALICE, pendingBen), model.getFilteredPersonList());

        Person emptyAlice = new PersonBuilder(ALICE).withPreorder().build();
        model.setPerson(ALICE, emptyAlice);
        assertEquals(List.of(pendingBen), model.getFilteredPersonList());
        assertEquals(criteria, model.getActiveSearchCriteria());

        parser.parseCommand("delete 1").execute(model);
        assertEquals(List.of(), model.getFilteredPersonList());
        assertEquals(List.of(emptyAlice, CARL), model.getAddressBook().getPersonList());
        assertEquals(criteria, model.getActiveSearchCriteria());
    }

    @Test
    public void execute_productSearchAfterTrack_replacesFilterAndListResetsIt() throws Exception {
        parser.parseCommand("track s/pending").execute(model);
        assertResults("find d/Pineapple Tarts", ALICE, BEN, CARL);
        assertEquals(2, model.getFilteredPersonList().getFirst().getPreorder().orElseThrow().getItems().size());

        assertResults("find d/No matching product");
        parser.parseCommand("list").execute(model);
        assertEquals(Model.SEARCH_CRITERIA_SHOW_ALL_PERSONS, model.getActiveSearchCriteria());
        assertEquals(List.of(ALICE, BEN, CARL), model.getFilteredPersonList());
    }

    private void assertResults(String command, Person... expectedPersons) throws Exception {
        CommandResult result = parser.parseCommand(command).execute(model);
        assertEquals(List.of(expectedPersons), model.getFilteredPersonList());
        assertEquals(new CommandResult(expectedPersons.length + " customer(s) listed!"), result);
    }
}
