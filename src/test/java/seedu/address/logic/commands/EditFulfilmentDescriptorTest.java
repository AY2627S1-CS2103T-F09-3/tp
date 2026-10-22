package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.PreorderCommand.EditFulfilmentDescriptor;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.testutil.EditFulfilmentDescriptorBuilder;

public class EditFulfilmentDescriptorTest {

    private static final String VALID_DATE = "2099-12-31";
    private static final String VALID_HANDOVER_NOTES = "Call on arrival";

    /**
     * Returns a descriptor that sets every fulfilment detail.
     */
    private static EditFulfilmentDescriptor createDescriptorWithAllDetails() {
        return new EditFulfilmentDescriptorBuilder().withDate(VALID_DATE)
                .withMethod(FulfilmentMethod.DELIVERY).withHandoverNotes(VALID_HANDOVER_NOTES).build();
    }

    @Test
    public void equals() {
        EditFulfilmentDescriptor descriptor = createDescriptorWithAllDetails();

        // same values -> returns true
        assertTrue(descriptor.equals(new EditFulfilmentDescriptor(descriptor)));

        // same object -> returns true
        assertTrue(descriptor.equals(descriptor));

        // null -> returns false
        assertFalse(descriptor.equals(null));

        // different types -> returns false
        assertFalse(descriptor.equals(5));

        // different date -> returns false
        EditFulfilmentDescriptor otherDescriptor = new EditFulfilmentDescriptor(descriptor);
        otherDescriptor.setDate(new FulfilmentDate("2098-01-15"));
        assertFalse(descriptor.equals(otherDescriptor));

        // different method -> returns false
        otherDescriptor = new EditFulfilmentDescriptor(descriptor);
        otherDescriptor.setMethod(FulfilmentMethod.COLLECTION);
        assertFalse(descriptor.equals(otherDescriptor));

        // different handover notes -> returns false
        otherDescriptor = new EditFulfilmentDescriptor(descriptor);
        otherDescriptor.setHandoverNotes(new HandoverNotes("Collect from side gate"));
        assertFalse(descriptor.equals(otherDescriptor));

        // clearing a field differs from leaving it unchanged -> returns false
        EditFulfilmentDescriptor unchangedDescriptor = new EditFulfilmentDescriptor();
        assertFalse(unchangedDescriptor.equals(new EditFulfilmentDescriptorBuilder().withoutDate().build()));
        assertFalse(unchangedDescriptor.equals(new EditFulfilmentDescriptorBuilder().withoutMethod().build()));
        assertFalse(unchangedDescriptor.equals(
                new EditFulfilmentDescriptorBuilder().withoutHandoverNotes().build()));
    }

    @Test
    public void hashCode_sameValues_sameHashCode() {
        EditFulfilmentDescriptor descriptor = createDescriptorWithAllDetails();
        assertEquals(descriptor.hashCode(), new EditFulfilmentDescriptor(descriptor).hashCode());
    }

    @Test
    public void hasNewValue() {
        // only clearing -> returns false
        assertFalse(new EditFulfilmentDescriptorBuilder().withoutDate().withoutMethod().withoutHandoverNotes()
                .build().hasNewValue());

        // any one new value -> returns true
        assertTrue(new EditFulfilmentDescriptorBuilder().withDate(VALID_DATE).build().hasNewValue());
        assertTrue(new EditFulfilmentDescriptorBuilder().withMethod(FulfilmentMethod.DELIVERY).build()
                .hasNewValue());
        assertTrue(new EditFulfilmentDescriptorBuilder().withHandoverNotes(VALID_HANDOVER_NOTES).build()
                .hasNewValue());
    }
}
