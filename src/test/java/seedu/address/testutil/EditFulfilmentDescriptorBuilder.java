package seedu.address.testutil;

import seedu.address.logic.commands.PreorderCommand.EditFulfilmentDescriptor;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;

/**
 * A utility class to help with building EditFulfilmentDescriptor objects.
 */
public class EditFulfilmentDescriptorBuilder {

    private EditFulfilmentDescriptor descriptor;

    public EditFulfilmentDescriptorBuilder() {
        descriptor = new EditFulfilmentDescriptor();
    }

    /**
     * Sets the {@code FulfilmentDate} of the {@code EditFulfilmentDescriptor} that we are building.
     */
    public EditFulfilmentDescriptorBuilder withDate(String date) {
        descriptor.setDate(new FulfilmentDate(date));
        return this;
    }

    /**
     * Clears the date in the {@code EditFulfilmentDescriptor} that we are building.
     */
    public EditFulfilmentDescriptorBuilder withoutDate() {
        descriptor.setDate(null);
        return this;
    }

    /**
     * Sets the {@code FulfilmentMethod} of the {@code EditFulfilmentDescriptor} that we are building.
     */
    public EditFulfilmentDescriptorBuilder withMethod(FulfilmentMethod method) {
        descriptor.setMethod(method);
        return this;
    }

    /**
     * Clears the method in the {@code EditFulfilmentDescriptor} that we are building.
     */
    public EditFulfilmentDescriptorBuilder withoutMethod() {
        descriptor.setMethod(null);
        return this;
    }

    /**
     * Sets the {@code HandoverNotes} of the {@code EditFulfilmentDescriptor} that we are building.
     */
    public EditFulfilmentDescriptorBuilder withHandoverNotes(String handoverNotes) {
        descriptor.setHandoverNotes(new HandoverNotes(handoverNotes));
        return this;
    }

    /**
     * Clears the handover notes in the {@code EditFulfilmentDescriptor} that we are building.
     */
    public EditFulfilmentDescriptorBuilder withoutHandoverNotes() {
        descriptor.setHandoverNotes(null);
        return this;
    }

    public EditFulfilmentDescriptor build() {
        return descriptor;
    }
}
