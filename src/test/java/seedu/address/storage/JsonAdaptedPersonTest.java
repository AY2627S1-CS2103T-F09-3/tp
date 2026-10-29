package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.PostalCode;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Quantity;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.ItemBuilder;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";
    private static final String VALID_POSTAL_CODE = BENSON.getPostalCode().value;
    private static final String VALID_LEADING_ZERO_POSTAL_CODE = "012345";
    private static final String INVALID_POSTAL_CODE = "12345";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_missingPostalCode_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                null, VALID_TAGS);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, PostalCode.class.getSimpleName()), person::toModelType);
    }

    @Test
    public void toModelType_validPostalCode_returnsPostalCode() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE, VALID_TAGS);
        assertEquals(VALID_POSTAL_CODE, person.toModelType().getPostalCode().value);
    }

    @Test
    public void toModelType_leadingZeroPostalCode_preservesLeadingZero() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_LEADING_ZERO_POSTAL_CODE, VALID_TAGS);
        assertEquals(VALID_LEADING_ZERO_POSTAL_CODE, person.toModelType().getPostalCode().value);
    }

    @Test
    public void toModelType_invalidPostalCode_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                INVALID_POSTAL_CODE, VALID_TAGS);
        assertThrows(IllegalValueException.class, PostalCode.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_personWithPreorder_returnsPersonWithPreorder() throws Exception {
        Person expected = new PersonBuilder(BENSON)
                .withPreorder("2 chocolate cakes", ItemStatus.PREPARED).build();

        JsonAdaptedPerson adapted = new JsonAdaptedPerson(expected);

        assertEquals(expected, adapted.toModelType());
        assertTrue(adapted.toModelType().getPreorder().isPresent());
    }

    @Test
    public void toModelType_personWithEachItemStatus_preservesStatus() throws Exception {
        for (ItemStatus status : ItemStatus.values()) {
            Person expected = new PersonBuilder(BENSON)
                    .withPreorder("2 chocolate cakes", status).build();
            assertEquals(expected, new JsonAdaptedPerson(expected).toModelType());
        }
    }

    @Test
    public void toModelType_personWithSeveralItems_keepsItemsInOrder() throws Exception {
        Person expected = new PersonBuilder(BENSON).withPreorder(
                new ItemBuilder().withName("Pineapple Tarts").withRemark("Less sugar").build(),
                new ItemBuilder().withName("Almond Cookies").withQuantity("1").build()).build();
        assertEquals(expected, new JsonAdaptedPerson(expected).toModelType());
    }

    @Test
    public void toModelType_preorderWithoutItems_returnsPersonWithEmptyPreorder() throws Exception {
        Person expected = new PersonBuilder(BENSON).withPreorder().build();
        assertEquals(expected, new JsonAdaptedPerson(expected).toModelType());
    }

    @Test
    public void toModelType_preorderWithoutItemsField_returnsPersonWithEmptyPreorder() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE,
                VALID_TAGS, List.of(new JsonAdaptedPreorder((List<JsonAdaptedItem>) null)), null);
        assertEquals(new PersonBuilder(BENSON).withPreorder().build(), person.toModelType());
    }

    @Test
    public void toModelType_invalidItem_throwsIllegalValueException() {
        JsonAdaptedItem invalidItem = new JsonAdaptedItem("Pineapple Tarts", "0", "PENDING", null);
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE,
                VALID_TAGS, List.of(new JsonAdaptedPreorder(List.of(invalidItem))), null);
        assertThrows(IllegalValueException.class, Quantity.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_moreThanOnePreorder_throwsIllegalValueException() {
        JsonAdaptedPreorder preorder = new JsonAdaptedPreorder(List.of());
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE,
                VALID_TAGS, List.of(preorder, preorder), null);
        assertThrows(IllegalValueException.class, JsonAdaptedPerson.MESSAGE_TOO_MANY_PREORDERS, person::toModelType);
    }

    @Test
    public void toModelType_nullPreorder_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE,
                VALID_TAGS, Arrays.asList((JsonAdaptedPreorder) null), null);
        assertThrows(IllegalValueException.class, JsonAdaptedPerson.MESSAGE_NULL_PREORDER, person::toModelType);
    }

    @Test
    public void toModelType_nullItem_throwsIllegalValueException() {
        JsonAdaptedPreorder preorder = new JsonAdaptedPreorder(Arrays.asList((JsonAdaptedItem) null));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE,
                VALID_TAGS, List.of(preorder), null);
        assertThrows(IllegalValueException.class, JsonAdaptedPreorder.MESSAGE_NULL_ITEM, person::toModelType);
    }

    @Test
    public void toModelType_legacyPreorder_returnsOneItemWithoutQuantity() throws Exception {
        JsonAdaptedLegacyPreorder legacyPreorder = new JsonAdaptedLegacyPreorder("2 chocolate cakes", "PREPARED");
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE,
                VALID_TAGS, null, legacyPreorder);

        Item expectedItem = new ItemBuilder().withName("2 chocolate cakes").withoutQuantity()
                .withStatus(ItemStatus.PREPARED).build();
        assertEquals(new PersonBuilder(BENSON).withPreorder(expectedItem).build(), person.toModelType());
    }

    @Test
    public void toModelType_invalidLegacyPreorder_throwsIllegalValueException() {
        JsonAdaptedLegacyPreorder legacyPreorder = new JsonAdaptedLegacyPreorder("2 chocolate cakes", "DONE");
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE,
                VALID_TAGS, null, legacyPreorder);
        assertThrows(IllegalValueException.class, ItemStatus.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_legacyAndCurrentPreorder_throwsIllegalValueException() {
        JsonAdaptedLegacyPreorder legacyPreorder = new JsonAdaptedLegacyPreorder("2 chocolate cakes", "PENDING");
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE,
                VALID_TAGS, List.of(new JsonAdaptedPreorder(List.of())), legacyPreorder);
        assertThrows(IllegalValueException.class, JsonAdaptedPerson.MESSAGE_TOO_MANY_PREORDERS, person::toModelType);
    }

    @Test
    public void toModelType_nameWithPunctuationAndExtraSpaces_returnsNormalizedPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson("  mArY-JaNe   O'BrIeN  ", VALID_PHONE, VALID_EMAIL,
                VALID_ADDRESS, VALID_POSTAL_CODE, VALID_TAGS);
        Person convertedPerson = person.toModelType();
        Person expectedPerson = new PersonBuilder(BENSON).withName("mArY-JaNe O'BrIeN").build();

        assertEquals(expectedPerson, convertedPerson);
        assertEquals("mArY-JaNe O'BrIeN", convertedPerson.getName().fullName);
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        for (String invalidName : new String[]{INVALID_NAME, "", "   ", "John2", "John\tTan", "-", "'", " - ' ",
            "José Tan"}) {
            JsonAdaptedPerson person =
                    new JsonAdaptedPerson(invalidName, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                            VALID_POSTAL_CODE, VALID_TAGS);
            assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS, person::toModelType);
        }
    }

    @Test
    public void toModelType_nameLengthAndFormatViolations_prioritizesLengthMessage() {
        for (int length : new int[]{99, 100}) {
            String invalidName = "  " + "A".repeat(length - 2) + "   !  ";
            JsonAdaptedPerson person = new JsonAdaptedPerson(invalidName, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                    VALID_POSTAL_CODE, VALID_TAGS);
            assertThrows(IllegalValueException.class, Name.MESSAGE_CONSTRAINTS, person::toModelType);
        }
        JsonAdaptedPerson person = new JsonAdaptedPerson("  " + "A".repeat(99) + "   !  ", VALID_PHONE, VALID_EMAIL,
                VALID_ADDRESS, VALID_POSTAL_CODE, VALID_TAGS);
        assertThrows(IllegalValueException.class,
                "Names must be at most 100 characters long after spaces are normalized.", person::toModelType);
    }

    @Test
    public void toModelType_emailLengthAndFormatViolations_prioritizesLengthMessage() {
        for (int length : new int[]{253, 254}) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, "a".repeat(length), VALID_ADDRESS,
                    VALID_POSTAL_CODE, VALID_TAGS);
            assertThrows(IllegalValueException.class, Email.MESSAGE_CONSTRAINTS, person::toModelType);
        }
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, "a".repeat(255), VALID_ADDRESS,
                VALID_POSTAL_CODE, VALID_TAGS);
        assertThrows(IllegalValueException.class, "Emails must be at most 254 characters long.", person::toModelType);
    }

    @Test
    public void toModelType_addressLengthAndFormatViolations_prioritizesLengthMessage() {
        for (int length : new int[]{299, 300}) {
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                    "A".repeat(length - 2) + "\nB", VALID_POSTAL_CODE, VALID_TAGS);
            assertThrows(IllegalValueException.class, Address.MESSAGE_CONSTRAINTS, person::toModelType);
        }
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL,
                "A".repeat(299) + "\nB", VALID_POSTAL_CODE, VALID_TAGS);
        assertThrows(IllegalValueException.class, "Addresses must be at most 300 characters long.",
                person::toModelType);
    }

    @Test
    public void toModelType_tagLengthAndFormatViolations_prioritizesLengthMessage() {
        for (int length : new int[]{29, 30}) {
            List<JsonAdaptedTag> invalidTags = List.of(new JsonAdaptedTag("A".repeat(length - 1) + "!"));
            JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                    VALID_POSTAL_CODE, invalidTags);
            assertThrows(IllegalValueException.class, Tag.MESSAGE_CONSTRAINTS, person::toModelType);
        }
        List<JsonAdaptedTag> invalidTags = List.of(new JsonAdaptedTag("A".repeat(30) + "!"));
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE, invalidTags);
        assertThrows(IllegalValueException.class, "Tag names must be at most 30 characters long.", person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_POSTAL_CODE, VALID_TAGS);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS,
                VALID_POSTAL_CODE, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS,
                        VALID_POSTAL_CODE, VALID_TAGS);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS,
                VALID_POSTAL_CODE, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS,
                        VALID_POSTAL_CODE, VALID_TAGS);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null,
                VALID_POSTAL_CODE, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                        VALID_POSTAL_CODE, invalidTags);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

}
