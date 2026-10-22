package seedu.address.testutil;

import java.util.List;
import java.util.Optional;

import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.Preorder;

/**
 * A utility class to help with building Preorder objects.
 */
public class PreorderBuilder {

    private List<Item> items;
    private Optional<FulfilmentDate> date;
    private Optional<FulfilmentMethod> method;
    private Optional<HandoverNotes> handoverNotes;

    /**
     * Creates a {@code PreorderBuilder} with no items and no fulfilment details.
     */
    public PreorderBuilder() {
        items = List.of();
        date = Optional.empty();
        method = Optional.empty();
        handoverNotes = Optional.empty();
    }

    /**
     * Initializes the PreorderBuilder with the data of {@code preorderToCopy}.
     */
    public PreorderBuilder(Preorder preorderToCopy) {
        items = preorderToCopy.getItems();
        date = preorderToCopy.getDate();
        method = preorderToCopy.getMethod();
        handoverNotes = preorderToCopy.getHandoverNotes();
    }

    /**
     * Sets the items of the {@code Preorder} that we are building.
     */
    public PreorderBuilder withItems(Item... items) {
        this.items = List.of(items);
        return this;
    }

    /**
     * Sets the {@code FulfilmentDate} of the {@code Preorder} that we are building.
     */
    public PreorderBuilder withDate(String date) {
        this.date = Optional.of(new FulfilmentDate(date));
        return this;
    }

    /**
     * Sets the {@code FulfilmentMethod} of the {@code Preorder} that we are building.
     */
    public PreorderBuilder withMethod(FulfilmentMethod method) {
        this.method = Optional.of(method);
        return this;
    }

    /**
     * Sets the {@code HandoverNotes} of the {@code Preorder} that we are building.
     */
    public PreorderBuilder withHandoverNotes(String handoverNotes) {
        this.handoverNotes = Optional.of(new HandoverNotes(handoverNotes));
        return this;
    }

    public Preorder build() {
        return new Preorder(items, date, method, handoverNotes);
    }

}
