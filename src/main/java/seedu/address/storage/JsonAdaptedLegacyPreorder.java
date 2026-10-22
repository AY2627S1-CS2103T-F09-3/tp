package seedu.address.storage;

import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.core.LogsCenter;

/**
 * Jackson-friendly version of a preorder saved by LeBake v1.3, which had free-text details and one status.
 * It is only read from older data files and never saved.
 */
class JsonAdaptedLegacyPreorder {

    private static final Logger logger = LogsCenter.getLogger(JsonAdaptedLegacyPreorder.class);

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
        logger.info("Converting a v1.3 preorder into one item without a quantity: " + details + " (" + status + ")");
        JsonAdaptedItem item = new JsonAdaptedItem(details, null, status, null);
        return new JsonAdaptedPreorder(List.of(item));
    }
}
