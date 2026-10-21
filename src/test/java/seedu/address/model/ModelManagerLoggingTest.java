package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;
import static seedu.address.model.search.TextContainsCriterion.Field.PHONE;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.TrackCommand;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TagCriterion;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.AddressBookBuilder;

/**
 * Tests logging at the shared criteria update point without changing the application's logger configuration.
 */
public class ModelManagerLoggingTest {
    private final List<LogRecord> logRecords = new ArrayList<>();
    private final Handler logHandler = new Handler() {
        @Override
        public void publish(LogRecord record) {
            logRecords.add(record);
        }

        @Override
        public void flush() {}

        @Override
        public void close() {}
    };

    private ModelManager modelManager;
    private Logger logger;
    private Level originalLevel;
    private boolean originalUseParentHandlers;

    @BeforeEach
    public void setUp() {
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build(),
                new UserPrefs());
        // Obtain the existing logger without resetting its handlers through LogsCenter.
        logger = Logger.getLogger("lebake." + ModelManager.class.getSimpleName());
        originalLevel = logger.getLevel();
        originalUseParentHandlers = logger.getUseParentHandlers();
        logger.setLevel(Level.INFO);
        logger.setUseParentHandlers(false);
        logger.addHandler(logHandler);
    }

    @AfterEach
    public void tearDown() {
        logger.removeHandler(logHandler);
        logger.setLevel(originalLevel);
        logger.setUseParentHandlers(originalUseParentHandlers);
    }

    @Test
    public void updateFilteredPersonList_combinedCriteria_logsTypesCountAndMatchingCustomers() {
        SearchCriteria criteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Alice"),
                new TextContainsCriterion(PHONE, "9435"), new TagCriterion(new Tag("friends"))));

        modelManager.updateFilteredPersonList(criteria);

        assertLogMessages("Search criteria applied: count=3, types=[TagCriterion, TextContainsCriterion], "
                + "matchingCustomers=1");
    }

    @Test
    public void updateFilteredPersonList_searchReplaced_logsUpdatedCounts() {
        modelManager.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Nobody"))));
        modelManager.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Benson"))));

        assertLogMessages(
                "Search criteria applied: count=1, types=[TextContainsCriterion], matchingCustomers=0",
                "Search criteria applied: count=1, types=[TextContainsCriterion], matchingCustomers=1");
    }

    @Test
    public void updateFilteredPersonList_emptyCriteria_logsResetAndAllCustomers() {
        modelManager.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Nobody"))));
        logRecords.clear();

        modelManager.updateFilteredPersonList(new SearchCriteria(List.of()));

        assertLogMessages("Search criteria reset: count=0, types=[], matchingCustomers=2");
    }

    @Test
    public void updateFilteredPersonList_sameResultsDifferentCriteria_logsBothUpdates() {
        modelManager.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Alice"))));
        modelManager.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(PHONE, "9435"))));

        assertLogMessages(
                "Search criteria applied: count=1, types=[TextContainsCriterion], matchingCustomers=1",
                "Search criteria applied: count=1, types=[TextContainsCriterion], matchingCustomers=1");
    }

    @Test
    public void updateFilteredPersonList_sameCriteriaInstance_logsBothUpdates() {
        SearchCriteria criteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Alice")));

        modelManager.updateFilteredPersonList(criteria);
        modelManager.updateFilteredPersonList(criteria);

        assertLogMessages(
                "Search criteria applied: count=1, types=[TextContainsCriterion], matchingCustomers=1",
                "Search criteria applied: count=1, types=[TextContainsCriterion], matchingCustomers=1");
    }

    @Test
    public void updateFilteredPersonList_nullCriteria_preservesSearchWithoutSuccessLog() {
        SearchCriteria criteria = new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Alice")));
        modelManager.updateFilteredPersonList(criteria);
        logRecords.clear();

        assertThrows(NullPointerException.class, () -> modelManager.updateFilteredPersonList(null));

        assertTrue(logRecords.isEmpty());
        assertEquals(criteria, modelManager.getActiveSearchCriteria());
        assertEquals(List.of(ALICE), modelManager.getFilteredPersonList());
    }

    @Test
    public void execute_findTrackAndList_logThroughCentralUpdate() {
        new FindCommand(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Alice")))).execute(modelManager);
        new TrackCommand(Optional.empty()).execute(modelManager);
        new ListCommand().execute(modelManager);

        assertLogMessages(
                "Search criteria applied: count=1, types=[TextContainsCriterion], matchingCustomers=1",
                "Search criteria applied: count=1, types=[PreorderStatusPredicate], matchingCustomers=0",
                "Search criteria reset: count=0, types=[], matchingCustomers=2");
    }

    private void assertLogMessages(String... expectedMessages) {
        assertEquals(List.of(expectedMessages), logRecords.stream().map(LogRecord::getMessage).toList());
        for (LogRecord record : logRecords) {
            assertEquals(Level.INFO, record.getLevel());
        }
    }
}
