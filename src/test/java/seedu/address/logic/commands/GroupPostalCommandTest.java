package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.model.search.TextContainsCriterion.Field.NAME;

import java.util.List;

import org.junit.jupiter.api.Test;

import javafx.collections.transformation.SortedList;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.search.SearchCriteria;
import seedu.address.model.search.TextContainsCriterion;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PreorderBuilder;

public class GroupPostalCommandTest {

    @Test
    public void execute_samePostalCodeWithoutDates_ordersNamesIgnoringCaseLikeDateSort() {
        Person ben = new PersonBuilder().withName("ben").withPostalCode("012345").build();
        Person zara = new PersonBuilder().withName("Zara").withPostalCode("012345").build();
        ModelManager model = new ModelManager();
        List.of(ben, zara).forEach(model::addPerson);
        AddressBook original = new AddressBook(model.getAddressBook());

        new GroupPostalCommand().execute(model);

        assertEquals(List.of(ben, zara), model.getFilteredPersonList());
        new SortCommand().execute(model);
        assertEquals(List.of(ben, zara), model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void execute_mixedCaseNames_ordersByFullPostalCodeThenNameIgnoringDates() {
        Person sameCodeAmy = new PersonBuilder().withName("Amy Lee").withPostalCode("012345")
                .withPreorder(new PreorderBuilder().withDate("2026-12-21").build()).build();
        Person sameCodeBen = new PersonBuilder().withName("ben Tan").withPostalCode("012345")
                .withPreorder(new PreorderBuilder().withDate("2026-12-21").build()).build();
        Person sameCodeZara = new PersonBuilder().withName("Zara Lim").withPostalCode("012345")
                .withPreorder(new PreorderBuilder().withDate("2026-12-20").build()).build();
        Person samePrefixLaterCode = new PersonBuilder().withName("Aaron Goh").withPostalCode("019876")
                .withPreorder(new PreorderBuilder().withDate("2026-12-19").build()).build();
        Person differentGroup = new PersonBuilder().withName("alice Ong").withPostalCode("531234")
                .withPreorder(new PreorderBuilder().withDate("2026-12-18").build()).build();
        ModelManager model = new ModelManager();
        List.of(sameCodeZara, differentGroup, samePrefixLaterCode, sameCodeBen, sameCodeAmy)
                .forEach(model::addPerson);
        AddressBook original = new AddressBook(model.getAddressBook());

        new SortCommand().execute(model);
        assertEquals(List.of(differentGroup, samePrefixLaterCode, sameCodeZara, sameCodeAmy, sameCodeBen),
                model.getFilteredPersonList());
        new GroupPostalCommand().execute(model);

        assertEquals(List.of(sameCodeAmy, sameCodeBen, sameCodeZara, samePrefixLaterCode, differentGroup),
                model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void execute_missingPostalCodes_ordersNamesIgnoringCaseAndKeepsMissingGroupLast() {
        Person missingBen = new PersonBuilder().withName("ben Tan").withPostalCode(null).build();
        Person missingZara = new PersonBuilder().withName("Zara Lim").withPostalCode(null).build();
        Person withCode = new PersonBuilder().withName("Zoe Lee").withPostalCode("999999").build();
        ModelManager model = new ModelManager();
        List.of(missingZara, missingBen, withCode).forEach(model::addPerson);
        AddressBook original = new AddressBook(model.getAddressBook());

        new GroupPostalCommand().execute(model);

        assertEquals(List.of(withCode, missingBen, missingZara), model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void execute_boundaryCodesAndMultipleMissingCodes_ordersByCodeThenName() {
        Person first = new PersonBuilder().withName("First").withPostalCode("000000").build();
        Person sameCodeAmy = new PersonBuilder().withName("Amy").withPostalCode("000001").build();
        Person sameCodeZoe = new PersonBuilder().withName("Zoe").withPostalCode("000001").build();
        Person lastCode = new PersonBuilder().withName("Last Code").withPostalCode("999999").build();
        Person missingAmy = new PersonBuilder().withName("Amy Missing").withPostalCode(null).build();
        Person missingZoe = new PersonBuilder().withName("Zoe Missing").withPostalCode(null).build();
        ModelManager model = new ModelManager();
        List.of(missingZoe, sameCodeZoe, lastCode, missingAmy, sameCodeAmy, first).forEach(model::addPerson);
        AddressBook original = new AddressBook(model.getAddressBook());

        CommandResult result = new GroupPostalCommand().execute(model);

        assertEquals(GroupPostalCommand.MESSAGE_SUCCESS, result.getFeedbackToUser());
        assertEquals(List.of(first, sameCodeAmy, sameCodeZoe, lastCode, missingAmy, missingZoe),
                model.getFilteredPersonList());
        assertEquals(original, model.getAddressBook());
    }

    @Test
    public void execute_repeatedGroupingAndList_preserveStoredOrder() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Missing").build());
        model.addPerson(new PersonBuilder().withName("Later").withPostalCode("654321").build());
        model.addPerson(new PersonBuilder().withName("Earlier").withPostalCode("012345").build());
        AddressBook original = new AddressBook(model.getAddressBook());

        new GroupPostalCommand().execute(model);
        new GroupPostalCommand().execute(model);

        assertEquals(original, model.getAddressBook());
        assertEquals("Earlier", model.getFilteredPersonList().getFirst().getName().fullName);
        new ListCommand().execute(model);
        assertEquals(original.getPersonList(), model.getFilteredPersonList());
    }
    @Test
    public void execute_groupsByPostalCodeAndMissingLast() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Missing").withPostalCode(null).build());
        model.addPerson(new PersonBuilder().withName("Later").withPostalCode("654321").build());
        model.addPerson(new PersonBuilder().withName("Earlier").withPostalCode("012345").build());
        model.addPerson(new PersonBuilder().withName("Same").withPostalCode("012345").build());

        new GroupPostalCommand().execute(model);

        assertEquals("012345", model.getFilteredPersonList().get(0).getPostalCode().value);
        assertEquals("012345", model.getFilteredPersonList().get(1).getPostalCode().value);
        assertEquals("654321", model.getFilteredPersonList().get(2).getPostalCode().value);
        assertEquals("Missing", model.getFilteredPersonList().get(3).getName().fullName);
    }

    @Test
    public void execute_groupsByPostalPrefix() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Later Prefix").withPostalCode("541111").build());
        model.addPerson(new PersonBuilder().withName("Earlier Prefix Late").withPostalCode("539999").build());
        model.addPerson(new PersonBuilder().withName("Earlier Prefix Early").withPostalCode("531234").build());

        new GroupPostalCommand().execute(model);

        assertEquals("531234", model.getFilteredPersonList().get(0).getPostalCode().value);
        assertEquals("539999", model.getFilteredPersonList().get(1).getPostalCode().value);
        assertEquals("541111", model.getFilteredPersonList().get(2).getPostalCode().value);
    }

    @Test
    public void execute_preservesActiveFilterAndDoesNotDuplicateCustomers() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Visible").withPostalCode("531234").build());
        model.addPerson(new PersonBuilder().withName("Hidden").withPostalCode("541234").build());
        model.updateFilteredPersonList(new SearchCriteria(List.of(new TextContainsCriterion(NAME, "Visible"))));

        new GroupPostalCommand().execute(model);

        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals("Visible", model.getFilteredPersonList().get(0).getName().fullName);
        assertFalse(model.getFilteredPersonList().stream()
                .anyMatch(person -> person.getName().fullName.equals("Hidden")));
    }

    @Test
    public void execute_setsPostalComparator() {
        ModelManager model = new ModelManager();

        new GroupPostalCommand().execute(model);

        assertTrue(model.getFilteredPersonList() instanceof SortedList<?>);
        SortedList<?> sortedPersons = (SortedList<?>) model.getFilteredPersonList();
        assertSame(GroupPostalCommand.POSTAL_COMPARATOR, sortedPersons.getComparator());
    }

    @Test
    public void execute_emptyModel_doesNotFail() {
        ModelManager model = new ModelManager(new seedu.address.model.AddressBook(), new UserPrefs());
        new GroupPostalCommand().execute(model);
        assertEquals(0, model.getFilteredPersonList().size());
    }
}
