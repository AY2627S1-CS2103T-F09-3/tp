package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.preorder.ItemName;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.PreorderStatus;

/**
 * Jackson-friendly version of {@link Preorder}.
 */
class JsonAdaptedPreorder {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Preorder's %s field is missing!";

    private final String details;
    private final String status;

    /**
     * Constructs a {@code JsonAdaptedPreorder} with the given preorder details and status.
     *
     * @param details the details of the preorder.
     * @param status the status of the preorder.
     */
    @JsonCreator
    public JsonAdaptedPreorder(@JsonProperty("details") String details,
            @JsonProperty("status") String status) {
        this.details = details;
        this.status = status;
    }

    /** Converts a model preorder into this Jackson-friendly format. */
    public JsonAdaptedPreorder(Preorder source) {
        details = source.getDetails().value;
        status = source.getStatus().name();
    }

    /** Converts this Jackson-friendly preorder into the model type. */
    public Preorder toModelType() throws IllegalValueException {
        if (details == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    ItemName.class.getSimpleName()));
        }
        if (!ItemName.isValidItemName(details)) {
            throw new IllegalValueException(ItemName.MESSAGE_CONSTRAINTS);
        }

        if (status == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT,
                    PreorderStatus.class.getSimpleName()));
        }
        if (!PreorderStatus.isValidStatus(status)) {
            throw new IllegalValueException(PreorderStatus.MESSAGE_CONSTRAINTS);
        }

        return new Preorder(new ItemName(details), PreorderStatus.valueOf(status));
    }
}
