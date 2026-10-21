package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;
import seedu.address.model.person.PostalCode;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.ItemStatus;

public class SampleDataUtilTest {

    @Test
    public void getSamplePersons_haveValidPostalCodesIncludingLeadingZero() {
        Person[] samplePersons = SampleDataUtil.getSamplePersons();
        assertTrue(Arrays.stream(samplePersons).allMatch(person -> person.getPostalCode() != null
                && PostalCode.isValidPostalCode(person.getPostalCode().value)));
        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> person.getPostalCode().value.startsWith("0")));
        assertTrue(Arrays.stream(samplePersons).map(person -> person.getPostalCode().value).distinct().count() > 1);
    }

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
    public void getSamplePersons_deliveryAndCollection_present() {
        Person[] samplePersons = SampleDataUtil.getSamplePersons();
        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> hasMethod(person, FulfilmentMethod.DELIVERY)));
        assertTrue(Arrays.stream(samplePersons).anyMatch(person -> hasMethod(person, FulfilmentMethod.COLLECTION)));
    }

    @Test
    public void getSamplePersons_severalDeliveriesWithUnfinishedItems_present() {
        long unfinishedDeliveryCount = Arrays.stream(SampleDataUtil.getSamplePersons())
                .filter(person -> hasMethod(person, FulfilmentMethod.DELIVERY))
                .filter(person -> person.getPreorder().get().getItems().stream()
                        .anyMatch(item -> item.getStatus() != ItemStatus.COMPLETED))
                .count();
        assertTrue(unfinishedDeliveryCount >= 2);
    }

    private static boolean hasMethod(Person person, FulfilmentMethod method) {
        return person.getPreorder().isPresent()
                && person.getPreorder().get().getMethod().equals(Optional.of(method));
    }

    @Test
    public void getSamplePersons_preorderWithSeveralItems_present() {
        assertTrue(Arrays.stream(SampleDataUtil.getSamplePersons())
                .anyMatch(person -> person.getPreorder()
                        .map(preorder -> preorder.getItems().size() > 1)
                        .orElse(false)));
    }
}
