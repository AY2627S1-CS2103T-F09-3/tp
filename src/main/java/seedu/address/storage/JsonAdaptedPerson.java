package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.PostalCode;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String MESSAGE_TOO_MANY_PREORDERS = "Person has more than one preorder!";
    public static final String MESSAGE_NULL_PREORDER = "Person's preorder list contains a null entry!";
    public static final String MESSAGE_NULL_TAG = "Person's tag list contains a null entry!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String postalCode;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    // Saved as a list so that customers can have more than one preorder in a later version
    private final List<JsonAdaptedPreorder> preorders = new ArrayList<>();

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     * A {@code legacyPreorder} from a v1.3 data file is added to {@code preorders} in the current format.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("postalCode") String postalCode, @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("preorders") List<JsonAdaptedPreorder> preorders,
            @JsonProperty("preorder") JsonAdaptedLegacyPreorder legacyPreorder) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.postalCode = postalCode;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        if (preorders != null) {
            this.preorders.addAll(preorders);
        }
        if (legacyPreorder != null) {
            this.preorders.add(legacyPreorder.toJsonAdaptedPreorder());
        }
    }

    /** Creates an adapted customer with a postal code and no preorder. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String postalCode,
            List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, postalCode, tags, null, null);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        postalCode = source.getPostalCode().value;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        source.getPreorder().ifPresent(preorder -> preorders.add(new JsonAdaptedPreorder(preorder)));
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException(MESSAGE_NULL_TAG);
            }
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.getValidationErrorMessage(name));
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.getValidationErrorMessage(email));
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.getValidationErrorMessage(address));
        }
        final Address modelAddress = new Address(address);
        if (postalCode == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, PostalCode.class.getSimpleName()));
        }
        if (!PostalCode.isValidPostalCode(postalCode)) {
            throw new IllegalValueException(PostalCode.MESSAGE_CONSTRAINTS);
        }
        final PostalCode modelPostalCode = new PostalCode(postalCode);

        final Set<Tag> modelTags = new HashSet<>(personTags);

        if (preorders.contains(null)) {
            throw new IllegalValueException(MESSAGE_NULL_PREORDER);
        }
        if (preorders.size() > 1) {
            throw new IllegalValueException(MESSAGE_TOO_MANY_PREORDERS);
        }
        final Optional<Preorder> modelPreorder = preorders.isEmpty()
                ? Optional.empty()
                : Optional.of(preorders.get(0).toModelType());

        return new Person(modelName, modelPhone, modelEmail, modelAddress, modelPostalCode, modelTags, modelPreorder);
    }

}
