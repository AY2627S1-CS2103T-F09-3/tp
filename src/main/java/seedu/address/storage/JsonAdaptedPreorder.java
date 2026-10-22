package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.HandoverNotes;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.Preorder;

/**
 * Jackson-friendly version of {@link Preorder}.
 */
class JsonAdaptedPreorder {

    public static final String MESSAGE_NULL_ITEM = "Preorder's item list contains a null entry!";

    private final List<JsonAdaptedItem> items = new ArrayList<>();
    private final String date; // null when no date has been set
    private final String method; // null when no method has been set
    private final String handoverNotes; // null when there are no handover notes

    /**
     * Constructs a {@code JsonAdaptedPreorder} with the given items and fulfilment details.
     * Data files saved before fulfilment details existed load with all three details unset.
     */
    @JsonCreator
    public JsonAdaptedPreorder(@JsonProperty("items") List<JsonAdaptedItem> items,
            @JsonProperty("date") String date, @JsonProperty("method") String method,
            @JsonProperty("handoverNotes") String handoverNotes) {
        if (items != null) {
            this.items.addAll(items);
        }
        this.date = date;
        this.method = method;
        this.handoverNotes = handoverNotes;
    }

    /**
     * Constructs a {@code JsonAdaptedPreorder} with the given items and no fulfilment details.
     */
    public JsonAdaptedPreorder(List<JsonAdaptedItem> items) {
        this(items, null, null, null);
    }

    /**
     * Converts a given {@code Preorder} into this class for Jackson use.
     */
    public JsonAdaptedPreorder(Preorder source) {
        items.addAll(source.getItems().stream()
                .map(JsonAdaptedItem::new)
                .toList());
        date = source.getDate().map(FulfilmentDate::toString).orElse(null);
        method = source.getMethod().map(FulfilmentMethod::name).orElse(null);
        handoverNotes = source.getHandoverNotes().map(notes -> notes.value).orElse(null);
    }

    /**
     * Converts this Jackson-friendly adapted preorder object into the model's {@code Preorder} object.
     *
     * @throws IllegalValueException if an item is null, or an item or fulfilment detail violates a data constraint.
     */
    public Preorder toModelType() throws IllegalValueException {
        final List<Item> modelItems = new ArrayList<>();
        for (JsonAdaptedItem item : items) {
            if (item == null) {
                throw new IllegalValueException(MESSAGE_NULL_ITEM);
            }
            modelItems.add(item.toModelType());
        }

        if (date != null && !FulfilmentDate.isValidDate(date)) {
            throw new IllegalValueException(FulfilmentDate.MESSAGE_CONSTRAINTS);
        }
        if (method != null && !FulfilmentMethod.isValidMethod(method)) {
            throw new IllegalValueException(FulfilmentMethod.MESSAGE_CONSTRAINTS);
        }
        if (handoverNotes != null && !HandoverNotes.isValidHandoverNotes(handoverNotes)) {
            throw new IllegalValueException(HandoverNotes.MESSAGE_CONSTRAINTS);
        }

        return new Preorder(modelItems, Optional.ofNullable(date).map(FulfilmentDate::new),
                Optional.ofNullable(method).map(FulfilmentMethod::valueOf),
                Optional.ofNullable(handoverNotes).map(HandoverNotes::new));
    }
}
