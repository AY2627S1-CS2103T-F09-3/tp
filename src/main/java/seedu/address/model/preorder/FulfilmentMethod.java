package seedu.address.model.preorder;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

/**
 * Represents how a customer receives their preorder.
 * A fixed set of values prevents variations such as "pickup" and "self-collect" from becoming different methods.
 */
public enum FulfilmentMethod {
    /** The baker delivers the preorder to the customer's address. */
    DELIVERY,
    /** The customer collects the preorder from the baker. */
    COLLECTION;

    public static final String MESSAGE_CONSTRAINTS =
            "Invalid method! Method must be DELIVERY or COLLECTION.";

    /**
     * Returns true if a given string is exactly the name of a method, such as "DELIVERY".
     */
    public static boolean isValidMethod(String test) {
        requireNonNull(test);
        return Arrays.stream(values()).anyMatch(method -> method.name().equals(test));
    }
}
