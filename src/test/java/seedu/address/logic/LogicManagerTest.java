package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.model.Model.SEARCH_CRITERIA_SHOW_ALL_PERSONS;
import static seedu.address.model.preorder.ItemStatus.PENDING;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.model.search.TextContainsCriterion.Field.PHONE;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.AMY;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.COMPLETED_DANIEL;
import static seedu.address.testutil.TypicalPersons.PENDING_BENSON;
import static seedu.address.testutil.TypicalPersons.PENDING_ELLE;
import static seedu.address.testutil.TypicalPersons.PREPARED_CARL;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.TrackCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.PreorderStatusPredicate;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TagCriterion;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.model.tag.Tag;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;

    @Test
    public void execute_groupPostal_savesOriginalOrderAndReloadsAllFields() throws Exception {
        Person later = new PersonBuilder().withName("Later").withPostalCode("654321").build();
        Person missing = new PersonBuilder().withName("Missing").build();
        Person earlier = new PersonBuilder().withName("Earlier").withPostalCode("012345")
                .withTags("regular").withPreorder("Chocolate cake", ItemStatus.PENDING).build();
        List<Person> savedOrder = List.of(later, missing, earlier);
        savedOrder.forEach(model::addPerson);

        logic.execute("group postal");

        assertEquals(List.of(earlier, later, missing), logic.getFilteredPersonList());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        ReadOnlyAddressBook reloaded = storage.readAddressBook().orElseThrow();
        assertEquals(savedOrder, reloaded.getPersonList());
        assertEquals("012345", reloaded.getPersonList().get(2).getPostalCode().value);
        assertEquals(earlier.getPreorder(), reloaded.getPersonList().get(2).getPreorder());
        assertEquals(null, reloaded.getPersonList().get(1).getPostalCode());
    }

    @Test
    public void execute_editAfterGrouping_updatesDisplayedPersonAndPreservesOtherFields() throws Exception {
        Person later = new PersonBuilder().withName("Later").withPostalCode("654321").build();
        Person earlier = new PersonBuilder().withName("Earlier").withPostalCode("012345")
                .withTags("regular").withPreorder("Chocolate cake", ItemStatus.PREPARED).build();
        model.addPerson(later);
        model.addPerson(earlier);
        logic.execute("group postal");

        logic.execute("edit 1 c/000001");

        Person edited = new PersonBuilder(earlier).withPostalCode("000001").build();
        assertEquals(List.of(later, edited), model.getAddressBook().getPersonList());
        assertEquals(List.of(edited, later), logic.getFilteredPersonList());
        assertThrows(ParseException.class, () -> logic.execute("edit 1 c/12345"));
        assertEquals(List.of(later, edited), model.getAddressBook().getPersonList());
    }

    @Test
    public void execute_trackGroupTagUntagDeleteOrder_preservesCorrectCustomerAndPostalCode() throws Exception {
        Person later = new PersonBuilder().withName("Later").withPostalCode("654321")
                .withPreorder("Almond cookies", ItemStatus.PENDING).build();
        Person earlier = new PersonBuilder().withName("Earlier").withPostalCode("012345")
                .withPreorder("Chocolate cake", ItemStatus.PREPARED).build();
        Person hidden = new PersonBuilder().withName("Hidden").withPostalCode("000001").build();
        model.addPerson(later);
        model.addPerson(earlier);
        model.addPerson(hidden);
        logic.execute("track");
        logic.execute("group postal");
        assertEquals(List.of(earlier, later), logic.getFilteredPersonList());

        logic.execute("tag 1 t/vip");
        logic.execute("untag 1 t/VIP");
        logic.execute("deleteorder 1");

        Person withoutPreorder = new PersonBuilder().withName("Earlier").withPostalCode("012345").build();
        assertEquals(List.of(later, withoutPreorder, hidden), model.getAddressBook().getPersonList());
        assertEquals(List.of(later), logic.getFilteredPersonList());
    }

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_searchCommands_updatesObservableCriteriaAndResetsOnList() throws Exception {
        setUpTrackPersons();
        assertEquals(SEARCH_CRITERIA_SHOW_ALL_PERSONS, logic.getActiveSearchCriteria());
        List<SearchCriteria> observedCriteria = new ArrayList<>();
        logic.activeSearchCriteriaProperty().addListener((observable, oldCriteria, newCriteria) ->
                observedCriteria.add(newCriteria));
        SearchCriteria findCriteria = new SearchCriteria(List.of(
                new TextContainsCriterion(NAME, "Alice"), new TagCriterion(new Tag("friends"))));
        SearchCriteria trackCriteria = new SearchCriteria(List.of(new PreorderStatusPredicate(Set.of(PENDING))));

        assertFilteringCommandSuccess("find n/Alice t/friends", "1 person(s) listed!", List.of(ALICE));
        assertEquals(findCriteria, logic.getActiveSearchCriteria());

        assertFilteringCommandSuccess("track s/pending", "2 order(s) for PENDING listed!",
                List.of(PENDING_ELLE, PENDING_BENSON));
        assertEquals(trackCriteria, logic.getActiveSearchCriteria());

        assertFilteringCommandSuccess("list", ListCommand.MESSAGE_SUCCESS,
                List.of(COMPLETED_DANIEL, PENDING_ELLE, ALICE, PREPARED_CARL, PENDING_BENSON));
        assertEquals(SEARCH_CRITERIA_SHOW_ALL_PERSONS, logic.getActiveSearchCriteria());
        assertEquals(List.of(findCriteria, trackCriteria, SEARCH_CRITERIA_SHOW_ALL_PERSONS), observedCriteria);
    }

    @Test
    public void execute_sameResultsDifferentSearch_updatesActiveCriteria() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find n/Alice", "1 person(s) listed!", List.of(ALICE));
        SearchCriteria phoneCriteria = new SearchCriteria(List.of(new TextContainsCriterion(PHONE, "9435")));

        assertFilteringCommandSuccess("find p/9435", "1 person(s) listed!", List.of(ALICE));

        assertEquals(phoneCriteria, logic.getActiveSearchCriteria());
        assertEquals(phoneCriteria, logic.activeSearchCriteriaProperty().get());
    }

    @Test
    public void execute_findWithoutMatches_retainsCriteriaUntilList() throws Exception {
        setUpTrackPersons();
        SearchCriteria criteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Nobody")));

        assertFilteringCommandSuccess("find n/Nobody", "0 person(s) listed!", List.of());
        assertEquals(criteria, logic.getActiveSearchCriteria());
        assertCommandFailurePreservesFilteredList("delete 1", CommandException.class,
                MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        logic.execute("list");

        assertEquals(SEARCH_CRITERIA_SHOW_ALL_PERSONS, logic.getActiveSearchCriteria());
        assertEquals(model.getAddressBook().getPersonList(), logic.getFilteredPersonList());
    }

    @Test
    public void execute_editLeavesCombinedSearch_nextEditUsesUpdatedDisplayedIndex() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find n/Meier t/friends", "2 person(s) listed!",
                List.of(COMPLETED_DANIEL, PENDING_BENSON));
        SearchCriteria criteria = logic.getActiveSearchCriteria();
        Person editedDaniel = new PersonBuilder(COMPLETED_DANIEL).withName("Daniel Wong").build();

        logic.execute("edit 1 n/Daniel Wong");

        assertEquals(List.of(PENDING_BENSON), logic.getFilteredPersonList());
        assertEquals(criteria, logic.getActiveSearchCriteria());
        assertCommandFailurePreservesFilteredList("edit 2 p/81234567", CommandException.class,
                MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);

        logic.execute("edit 1 p/81234567");

        Person editedBenson = new PersonBuilder(PENDING_BENSON).withPhone("81234567").build();
        assertEquals(List.of(editedBenson), logic.getFilteredPersonList());
        assertEquals(List.of(editedDaniel, PENDING_ELLE, ALICE, PREPARED_CARL, editedBenson),
                model.getAddressBook().getPersonList());
        assertEquals(criteria, logic.getActiveSearchCriteria());
    }

    @Test
    public void execute_customerAndPreorderChanges_preservesTrackingCriteriaAndRefreshesResults() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("track s/pending", "2 order(s) for PENDING listed!",
                List.of(PENDING_ELLE, PENDING_BENSON));
        SearchCriteria criteria = logic.getActiveSearchCriteria();
        List<SearchCriteria> observedCriteria = new ArrayList<>();
        logic.activeSearchCriteriaProperty().addListener((observable, oldCriteria, newCriteria) ->
                observedCriteria.add(newCriteria));
        Person newPerson = new PersonBuilder().withName("Nora Lee").withPhone("81112222")
                .withEmail("nora@example.com").withAddress("12 Sample Road").withPostalCode("123456").build();

        logic.execute("add n/Nora Lee p/81112222 e/nora@example.com a/12 Sample Road c/123456");

        assertEquals(List.of(PENDING_ELLE, PENDING_BENSON), logic.getFilteredPersonList());
        assertEquals(criteria, logic.getActiveSearchCriteria());

        logic.execute("edit 1 n/Elle Wong p/81234567");

        Person editedElle = new PersonBuilder(PENDING_ELLE).withName("Elle Wong").withPhone("81234567").build();
        assertEquals(List.of(editedElle, PENDING_BENSON), logic.getFilteredPersonList());
        assertEquals(criteria, logic.getActiveSearchCriteria());

        Person completedElle = new PersonBuilder(editedElle).withPreorder("1 strawberry cake",
                ItemStatus.COMPLETED).build();
        model.setPerson(editedElle, completedElle);

        assertEquals(List.of(PENDING_BENSON), logic.getFilteredPersonList());
        assertEquals(criteria, logic.getActiveSearchCriteria());

        logic.execute("deleteorder 1");

        assertEquals(List.of(COMPLETED_DANIEL, completedElle, ALICE, PREPARED_CARL, BENSON, newPerson),
                model.getAddressBook().getPersonList());
        assertEquals(List.of(), logic.getFilteredPersonList());
        assertEquals(criteria, logic.getActiveSearchCriteria());
        assertEquals(List.of(), observedCriteria);
    }

    @Test
    public void execute_tagChanges_preservesCriteriaAndRefreshesDisplayedIndexes() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find t/friends", "3 person(s) listed!",
                List.of(COMPLETED_DANIEL, ALICE, PENDING_BENSON));
        SearchCriteria criteria = logic.getActiveSearchCriteria();

        logic.execute("untag 1 t/friends");

        assertEquals(List.of(ALICE, PENDING_BENSON), logic.getFilteredPersonList());
        assertEquals(criteria, logic.getActiveSearchCriteria());

        logic.execute("tag 1 t/VIP");

        Person untaggedDaniel = new PersonBuilder(COMPLETED_DANIEL).withTags().build();
        Person taggedAlice = new PersonBuilder(ALICE).withTags("friends", "VIP").build();
        assertEquals(List.of(taggedAlice, PENDING_BENSON), logic.getFilteredPersonList());
        assertEquals(List.of(untaggedDaniel, PENDING_ELLE, taggedAlice, PREPARED_CARL, PENDING_BENSON),
                model.getAddressBook().getPersonList());
        assertEquals(criteria, logic.getActiveSearchCriteria());
    }

    @Test
    public void execute_findThenTrack_listsActivePreordersFromAllCustomers() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find n/Alice", "1 person(s) listed!", List.of(ALICE));

        assertFilteringCommandSuccess("track", "3 order(s) listed!",
                List.of(PENDING_ELLE, PREPARED_CARL, PENDING_BENSON));
    }

    @Test
    public void execute_findThenTrackStatus_listsMatchingPreordersFromAllCustomers() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find n/Carl", "1 person(s) listed!", List.of(PREPARED_CARL));

        assertFilteringCommandSuccess("track s/pending", "2 order(s) for PENDING listed!",
                List.of(PENDING_ELLE, PENDING_BENSON));
    }

    @Test
    public void execute_combinedCustomerSearch_requiresAllCriteria() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find t/FRIENDS a/jurong", "1 person(s) listed!", List.of(ALICE));
        assertFilteringCommandSuccess("find p/9876 e/EXAMPLE.COM", "1 person(s) listed!", List.of(PENDING_BENSON));
        assertFilteringCommandSuccess("find n/Alice t/owesMoney", "0 person(s) listed!", List.of());
    }

    @Test
    public void execute_successiveFindCommands_replacesPreviousFilter() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find n/Alice", "1 person(s) listed!", List.of(ALICE));
        assertFilteringCommandSuccess("find t/friends", "3 person(s) listed!",
                List.of(COMPLETED_DANIEL, ALICE, PENDING_BENSON));
    }

    @Test
    public void execute_trackThenFind_searchesAllCustomers() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("track s/completed", "1 order(s) for COMPLETED listed!",
                List.of(COMPLETED_DANIEL));
        assertFilteringCommandSuccess("find n/Alice", "1 person(s) listed!", List.of(ALICE));
    }

    @Test
    public void execute_findThenList_restoresAllCustomers() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find a/Jurong t/friends", "1 person(s) listed!", List.of(ALICE));
        assertFilteringCommandSuccess("list", ListCommand.MESSAGE_SUCCESS,
                List.of(COMPLETED_DANIEL, PENDING_ELLE, ALICE, PREPARED_CARL, PENDING_BENSON));
    }

    @Test
    public void execute_invalidFindFormat_preservesDataAndFilteredList() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find n/Alice", "1 person(s) listed!", List.of(ALICE));
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);

        for (String input : List.of("find", "find Alice", "find Alice t/friends", "find n/",
                "find n/Alice p/", "find n/Alice x/value", "find s/pending", "find n/Alice\ts/pending")) {
            assertCommandFailurePreservesFilteredList(input, ParseException.class, expectedMessage);
        }
    }

    @Test
    public void execute_duplicateFindPrefix_preservesDataAndFilteredList() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find n/Alice", "1 person(s) listed!", List.of(ALICE));
        assertCommandFailurePreservesFilteredList("find t/friends t/owesMoney", ParseException.class,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_TAG));
    }

    @Test
    public void execute_invalidFindTag_preservesDataAndFilteredList() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find n/Alice", "1 person(s) listed!", List.of(ALICE));
        assertCommandFailurePreservesFilteredList("find t/regular customer", ParseException.class,
                Tag.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void execute_combinedFindThenDelete_targetsDisplayedIndex() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("find a/Jurong t/friends", "1 person(s) listed!", List.of(ALICE));

        CommandResult result = logic.execute("delete 1");

        assertEquals(new CommandResult(String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(ALICE))), result);
        assertEquals(List.of(COMPLETED_DANIEL, PENDING_ELLE, PREPARED_CARL, PENDING_BENSON),
                model.getAddressBook().getPersonList());
        assertEquals(List.of(), logic.getFilteredPersonList());
    }

    @Test
    public void execute_successiveTrackCommands_replacesPreviousFilter() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("track s/COMPLETED", "1 order(s) for COMPLETED listed!",
                List.of(COMPLETED_DANIEL));
        assertFilteringCommandSuccess("track s/PENDING", "2 order(s) for PENDING listed!",
                List.of(PENDING_ELLE, PENDING_BENSON));
        assertFilteringCommandSuccess("track s/PREPARED", "1 order(s) for PREPARED listed!",
                List.of(PREPARED_CARL));
        assertFilteringCommandSuccess("track", "3 order(s) listed!",
                List.of(PENDING_ELLE, PREPARED_CARL, PENDING_BENSON));
    }

    @Test
    public void execute_trackThenList_restoresAllCustomersInOriginalOrder() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("track", "3 order(s) listed!",
                List.of(PENDING_ELLE, PREPARED_CARL, PENDING_BENSON));

        assertFilteringCommandSuccess("list", ListCommand.MESSAGE_SUCCESS,
                List.of(COMPLETED_DANIEL, PENDING_ELLE, ALICE, PREPARED_CARL, PENDING_BENSON));
    }

    @Test
    public void execute_invalidTrackFormat_preservesDataAndFilteredList() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("track s/COMPLETED", "1 order(s) for COMPLETED listed!",
                List.of(COMPLETED_DANIEL));

        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, TrackCommand.MESSAGE_USAGE);
        for (String input : List.of("track 1", "track PENDING", "track p/123")) {
            assertCommandFailurePreservesFilteredList(input, ParseException.class, expectedMessage);
        }
    }

    @Test
    public void execute_invalidTrackStatus_preservesDataAndFilteredList() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("track s/COMPLETED", "1 order(s) for COMPLETED listed!",
                List.of(COMPLETED_DANIEL));

        for (String input : List.of("track s/", "track s/UNKNOWN", "track s/PENDING p/123",
                "track s/PENDING extra")) {
            assertCommandFailurePreservesFilteredList(input, ParseException.class, ItemStatus.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void execute_duplicateTrackStatuses_preservesDataAndFilteredList() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("track s/COMPLETED", "1 order(s) for COMPLETED listed!",
                List.of(COMPLETED_DANIEL));

        assertCommandFailurePreservesFilteredList("track s/PENDING s/PREPARED", ParseException.class,
                "Multiple values specified for the following single-valued field(s): s/");
    }

    @Test
    public void execute_trackThenDelete_deletesCustomerAtDisplayedIndex() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("track", "3 order(s) listed!",
                List.of(PENDING_ELLE, PREPARED_CARL, PENDING_BENSON));

        CommandResult result = logic.execute("delete 1");

        String expectedMessage = String.format(DeleteCommand.MESSAGE_DELETE_PERSON_SUCCESS,
                Messages.format(PENDING_ELLE));
        assertEquals(new CommandResult(expectedMessage), result);
        assertEquals(List.of(COMPLETED_DANIEL, ALICE, PREPARED_CARL, PENDING_BENSON),
                model.getAddressBook().getPersonList());
        assertEquals(List.of(PREPARED_CARL, PENDING_BENSON), logic.getFilteredPersonList());
    }

    @Test
    public void execute_trackThenDeleteHiddenIndex_preservesDataAndFilteredList() throws Exception {
        setUpTrackPersons();
        assertFilteringCommandSuccess("track", "3 order(s) listed!",
                List.of(PENDING_ELLE, PREPARED_CARL, PENDING_BENSON));

        assertCommandFailurePreservesFilteredList("delete 4", CommandException.class,
                MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    private void setUpTrackPersons() {
        model.setAddressBook(new AddressBookBuilder().withPerson(COMPLETED_DANIEL).withPerson(PENDING_ELLE)
                .withPerson(ALICE).withPerson(PREPARED_CARL).withPerson(PENDING_BENSON).build());
    }

    private void assertFilteringCommandSuccess(String inputCommand, String expectedMessage,
            List<Person> expectedPersons) throws CommandException, ParseException {
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());

        assertEquals(new CommandResult(expectedMessage), logic.execute(inputCommand));
        assertEquals(expectedPersons, logic.getFilteredPersonList());
        assertEquals(originalAddressBook, model.getAddressBook());
    }

    private void assertCommandFailurePreservesFilteredList(String inputCommand,
            Class<? extends Throwable> expectedException, String expectedMessage) {
        AddressBook originalAddressBook = new AddressBook(model.getAddressBook());
        List<Person> originalFilteredPersons = List.copyOf(logic.getFilteredPersonList());
        SearchCriteria originalCriteria = logic.getActiveSearchCriteria();

        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(originalAddressBook, model.getAddressBook());
        assertEquals(originalFilteredPersons, logic.getFilteredPersonList());
        assertEquals(originalCriteria, logic.getActiveSearchCriteria());
        assertEquals(originalCriteria, logic.activeSearchCriteriaProperty().get());
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.updateFilteredPersonList(model.getActiveSearchCriteria());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + " c/123456";
        Person expectedPerson = new PersonBuilder(AMY).withPostalCode("123456").withTags().build();
        ModelManager expectedModel = new ModelManager();
        expectedModel.addPerson(expectedPerson);
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }
}
