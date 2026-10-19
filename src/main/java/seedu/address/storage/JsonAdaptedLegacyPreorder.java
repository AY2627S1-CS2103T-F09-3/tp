package seedu.address.storage;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson-friendly version of a preorder saved by LeBake v1.3, which had free-text details and one status.
 * It is only read from older data files and never saved.
 */
class JsonAdaptedLegacyPreorder {

    // v1.3 preorders did not record a quantity
    public static final String DEFAULT_QUANTITY = "1";

    private final String details;
    private final String status;

    /**
     * Constructs a {@code JsonAdaptedLegacyPreorder} with the given details and status.
     */
    @JsonCreator
    public JsonAdaptedLegacyPreorder(@JsonProperty("details") String details,
            @JsonProperty("status") String status) {
        this.details = details;
        this.status = status;
    }

    /**
     * Returns this preorder in the current format: one item named after the details, with quantity 1.
     * The item is checked later, when the returned preorder is converted to the model type.
     */
    public JsonAdaptedPreorder toJsonAdaptedPreorder() {
        JsonAdaptedItem item = new JsonAdaptedItem(details, DEFAULT_QUANTITY, status, null);
        return new JsonAdaptedPreorder(List.of(item));
    }
}
