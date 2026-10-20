package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

public class SampleDataUtilTest {

    @Test
    public void getSampleAddressBook_containsAllSamplePersons() {
        ReadOnlyAddressBook sampleAddressBook = SampleDataUtil.getSampleAddressBook();
        assertEquals(Arrays.asList(SampleDataUtil.getSamplePersons()), sampleAddressBook.getPersonList());
    }

    @Test
    public void getSamplePersons_someHavePreorders_othersDoNot() {
        Person[] samplePersons = SampleDataUtil.getSamplePersons();
        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> person.getPreorder().isPresent()));
        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> person.getPreorder().isEmpty()));
    }

    @Test
    public void getSamplePersons_preorderWithoutItems_present() {
        assertTrue(Arrays.stream(SampleDataUtil.getSamplePersons())
                .anyMatch(person -> person.getPreorder()
                        .map(preorder -> preorder.getItems().isEmpty())
                        .orElse(false)));
    }

    @Test
    public void getSamplePersons_preorderWithSeveralItems_present() {
        assertTrue(Arrays.stream(SampleDataUtil.getSamplePersons())
                .anyMatch(person -> person.getPreorder()
                        .map(preorder -> preorder.getItems().size() > 1)
                        .orElse(false)));
    }
}
