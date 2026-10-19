package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.Preorder;

/**
 * Jackson-friendly version of {@link Preorder}.
 */
class JsonAdaptedPreorder {

    private final List<JsonAdaptedItem> items = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPreorder} with the given items.
     */
    @JsonCreator
    public JsonAdaptedPreorder(@JsonProperty("items") List<JsonAdaptedItem> items) {
        if (items != null) {
            this.items.addAll(items);
        }
    }

    /**
     * Converts a given {@code Preorder} into this class for Jackson use.
     */
    public JsonAdaptedPreorder(Preorder source) {
        items.addAll(source.getItems().stream()
                .map(JsonAdaptedItem::new)
                .toList());
    }

    /**
     * Converts this Jackson-friendly adapted preorder object into the model's {@code Preorder} object.
     *
     * @throws IllegalValueException if any of the adapted items violates a data constraint.
     */
    public Preorder toModelType() throws IllegalValueException {
        final List<Item> modelItems = new ArrayList<>();
        for (JsonAdaptedItem item : items) {
            modelItems.add(item.toModelType());
        }
        return new Preorder(modelItems);
    }
}
