package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.util.SampleDataUtil;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;
import seedu.address.testutil.PreorderBuilder;

public class DeliveryPreparationSummaryTest {
    @Test
    public void constructor_includesOnlyOutstandingDeliveriesAndCountsCustomersOnce() {
        PreorderBuilder delivery = new PreorderBuilder().withMethod(FulfilmentMethod.DELIVERY);
        Person mixed = new PersonBuilder().withName("Mixed").withPostalCode("012345").withPreorder(delivery
                .withItems(new ItemBuilder().withStatus(ItemStatus.COMPLETED).build(), new ItemBuilder().build(),
                        new ItemBuilder().withStatus(ItemStatus.PREPARED).build()).build()).build();
        Person missingCode = new PersonBuilder().withName("Missing Postal").withPreorder(delivery
                .withItems(new ItemBuilder().withStatus(ItemStatus.PREPARED).build()).build()).build();
        Person samePrefix = new PersonBuilder(mixed).withName("Same Prefix").withPostalCode("019876").build();
        Person completed = new PersonBuilder().withName("Completed").withPreorder(delivery
                .withItems(new ItemBuilder().withStatus(ItemStatus.COMPLETED).build()).build()).build();
        Person empty = new PersonBuilder().withName("Empty").withPreorder(delivery.withItems().build()).build();
        Person collection = new PersonBuilder().withName("Collection").withPreorder(delivery
                .withMethod(FulfilmentMethod.COLLECTION).withItems(new ItemBuilder().build()).build()).build();
        Person unset = new PersonBuilder().withName("Unset Method").withPreorder(new ItemBuilder().build()).build();
        List<Person> customers = List.of(collection, missingCode, completed, mixed, samePrefix, empty, unset,
                new PersonBuilder().withName("No Preorder").build());

        DeliveryPreparationSummary summary = new DeliveryPreparationSummary(customers);

        assertEquals(List.of(mixed, samePrefix, missingCode), summary.getStops());
        assertEquals(3, summary.getStopCount());
        assertEquals(2, summary.getPostalGroupCount());
        assertEquals("012345", summary.getStops().getFirst().getPostalCode().value);
        assertThrows(UnsupportedOperationException.class, () -> summary.getStops().clear());
        assertEquals(collection, customers.getFirst());
    }

    @Test
    public void constructor_sampleData_hasTwoStopsInTwoGroups() {
        DeliveryPreparationSummary summary =
                new DeliveryPreparationSummary(Arrays.asList(SampleDataUtil.getSamplePersons()));
        assertEquals(2, summary.getStopCount());
        assertEquals(2, summary.getPostalGroupCount());
        assertEquals(List.of("Alex Yeoh", "Charlotte Oliveiro"),
                summary.getStops().stream().map(person -> person.getName().fullName).toList());
    }

    @Test
    public void constructor_emptyInput_hasNoStopsOrGroups() {
        DeliveryPreparationSummary summary = new DeliveryPreparationSummary(List.of());
        assertEquals(0, summary.getStopCount());
        assertEquals(0, summary.getPostalGroupCount());
        assertThrows(NullPointerException.class, () -> new DeliveryPreparationSummary(null));
        assertThrows(NullPointerException.class, () -> new DeliveryPreparationSummary(Arrays.asList((Person) null)));
    }
}
