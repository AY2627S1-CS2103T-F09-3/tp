package seedu.address.model.preorder;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents a customer's current pre-order: what was ordered and how far it has progressed.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Preorder {

    private final PreorderDetails details;
    private final PreorderStatus status;

    /**
     * Every field must be present and not null.
     */
    public Preorder(PreorderDetails details, PreorderStatus status) {
        requireAllNonNull(details, status);
        this.details = details;
        this.status = status;
    }

    public PreorderDetails getDetails() {
        return details;
    }

    public PreorderStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Preorder otherPreorder)) {
            return false;
        }

        return details.equals(otherPreorder.details)
                && status == otherPreorder.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(details, status);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("details", details)
                .add("status", status)
                .toString();
    }

}
