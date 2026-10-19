package seedu.address.storage;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Jackson-friendly version of a preorder saved by LeBake v1.3, which had free-text details and one status.
 * It is only read from older data files and never saved.
 */
class JsonAdaptedLegacyPreorder {

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
     * Returns this preorder in the current format: one item named after the details.
     * The quantity is left empty, as v1.3 did not record one and guessing it from the details could be wrong.
     * The item is checked later, when the returned preorder is converted to the model type.
     */
    public JsonAdaptedPreorder toJsonAdaptedPreorder() {
        JsonAdaptedItem item = new JsonAdaptedItem(details, null, status, null);
        return new JsonAdaptedPreorder(List.of(item));
    }
}
