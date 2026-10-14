package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.preorder.PreorderDetails;
import seedu.address.model.preorder.PreorderStatus;

/**
 * Jackson-friendly version of {@link Preorder}.
 */
class JsonAdaptedPreorder {
    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Preorder's %s field is missing!";

    private final String preorderDetails;
    private final String preorderStatus;

    /**
     * Constructs a {@code JsonAdaptedPreorder} with the given preorder details and status.
     */
    @JsonCreator
    public JsonAdaptedPreorder(@JsonProperty("preorderDetails") String preorderDetails,
            @JsonProperty("preorderStatus") String preorderStatus) {
        this.preorderDetails = preorderDetails;
        this.preorderStatus = preorderStatus;
    }

    /**
     * Converts a given {@code Preorder} into this class for Jackson use.
     */
    public JsonAdaptedPreorder(Preorder source) {
        this.preorderDetails = source.getDetails().value;
        this.preorderStatus = source.getStatus().name();
    }

    /**
     * Converts this Jackson-friendly adapted preorder object into the model's {@code Preorder} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted preorder.
     */
    public Preorder toModelType() throws IllegalValueException {
        if (preorderDetails == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "details"));
        }
        if (!PreorderDetails.isValidDetails(preorderDetails)) {
            throw new IllegalValueException(PreorderDetails.MESSAGE_CONSTRAINTS);
        }
        PreorderDetails modelPreorderDetails = new PreorderDetails(preorderDetails);

        if (preorderStatus == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "status"));
        }
        if (!PreorderStatus.isValidStatus(preorderStatus)) {
            throw new IllegalValueException(PreorderStatus.MESSAGE_CONSTRAINTS);
        }
        PreorderStatus modelPreorderStatus = PreorderStatus.valueOf(preorderStatus);

        return new Preorder(modelPreorderDetails, modelPreorderStatus);
    }

}
