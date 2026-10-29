package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_MARY_JANE_WITH_EXTRA_SPACES;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.POSTAL_CODE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_MARY_JANE;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_POSTAL_CODE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.PostalCode;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private static final String VALID_FIELDS = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
            + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB;

    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Person expectedPerson = new PersonBuilder(BOB).withPostalCode("654321").withTags().build();

        // whitespace only preamble
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB, new AddCommand(expectedPerson));

        // different field order
        assertParseSuccess(parser, ADDRESS_DESC_BOB + EMAIL_DESC_BOB + PHONE_DESC_BOB + NAME_DESC_BOB
                + POSTAL_CODE_DESC_BOB,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_nameWithPunctuationAndExtraSpaces_success() {
        Person expectedPerson = new PersonBuilder(BOB).withPostalCode("654321")
                .withName(VALID_NAME_MARY_JANE).withTags().build();

        assertParseSuccess(parser, NAME_DESC_MARY_JANE_WITH_EXTRA_SPACES + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_nameLengthBoundaries_checksNormalizedLength() {
        for (int length : new int[]{99, 100}) {
            String normalizedName = "A".repeat(length - 2) + " B";
            Person expectedPerson = new PersonBuilder(BOB).withName(normalizedName)
                    .withPostalCode("654321").withTags().build();
            assertParseSuccess(parser, " n/  " + "A".repeat(length - 2) + "   B  "
                    + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB,
                    new AddCommand(expectedPerson));
        }
        assertParseFailure(parser, " n/  " + "A".repeat(99) + "   B  "
                + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB,
                Name.MESSAGE_LENGTH_CONSTRAINTS);
    }

    @Test
    public void parse_emailLengthBoundaries_checksTrimmedLength() {
        for (int length : new int[]{253, 254}) {
            String email = createEmailWithLength(length);
            Person expectedPerson = new PersonBuilder(BOB).withEmail(email)
                    .withPostalCode("654321").withTags().build();
            assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " e/  " + email + "  "
                    + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB, new AddCommand(expectedPerson));
        }
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + " e/  " + createEmailWithLength(255) + "  "
                + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB, Email.MESSAGE_LENGTH_CONSTRAINTS);
    }

    @Test
    public void parse_addressLengthBoundaries_checksTrimmedLength() {
        for (int length : new int[]{299, 300}) {
            String address = "A".repeat(length);
            Person expectedPerson = new PersonBuilder(BOB).withAddress(address)
                    .withPostalCode("654321").withTags().build();
            assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/  " + address + "  "
                    + POSTAL_CODE_DESC_BOB, new AddCommand(expectedPerson));
        }
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/  " + "A".repeat(301) + "  "
                + POSTAL_CODE_DESC_BOB, Address.MESSAGE_LENGTH_CONSTRAINTS);
    }

    @Test
    public void parse_invalidNames_failure() {
        for (String invalidName : new String[]{"", "   ", "John2", "John@Tan", "John\tTan", "-", "'", " - ' ",
            "José Tan"}) {
            assertParseFailure(parser, " " + PREFIX_NAME + invalidName + PHONE_DESC_BOB + EMAIL_DESC_BOB
                    + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB, Name.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_repeatedPrefix_failure() {
        String validExpectedPersonString = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB;

        // multiple names
        assertParseFailure(parser, NAME_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // multiple phones
        assertParseFailure(parser, PHONE_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // multiple emails
        assertParseFailure(parser, EMAIL_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // multiple addresses
        assertParseFailure(parser, ADDRESS_DESC_AMY + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // multiple postal codes
        assertParseFailure(parser, POSTAL_CODE_DESC_BOB + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_POSTAL_CODE));

        // multiple fields repeated
        assertParseFailure(parser,
                validExpectedPersonString + PHONE_DESC_AMY + EMAIL_DESC_AMY + NAME_DESC_AMY + ADDRESS_DESC_AMY
                        + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME, PREFIX_ADDRESS, PREFIX_EMAIL, PREFIX_PHONE,
                        PREFIX_POSTAL_CODE));

        // invalid value followed by valid value

        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, INVALID_EMAIL_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, INVALID_PHONE_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, INVALID_ADDRESS_DESC + validExpectedPersonString,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));

        // valid value followed by invalid value

        // invalid name
        assertParseFailure(parser, validExpectedPersonString + INVALID_NAME_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));

        // invalid email
        assertParseFailure(parser, validExpectedPersonString + INVALID_EMAIL_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));

        // invalid phone
        assertParseFailure(parser, validExpectedPersonString + INVALID_PHONE_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));

        // invalid address
        assertParseFailure(parser, validExpectedPersonString + INVALID_ADDRESS_DESC,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ADDRESS));
    }

    @Test
    public void parse_addressContainingUnrecognizedPrefixes_success() {
        String address = "Block 123 c/o Alice x/foo";
        Person expectedPerson = new PersonBuilder(BOB).withPostalCode("654321").withAddress(address).withTags().build();
        assertParseSuccess(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + " a/" + address
                + POSTAL_CODE_DESC_BOB,
                new AddCommand(expectedPerson));
    }

    @Test
    public void parse_unsupportedPrefixesInConstrainedFields_namesPrefix() {
        for (String field : new String[]{NAME_DESC_BOB, PHONE_DESC_BOB, EMAIL_DESC_BOB, POSTAL_CODE_DESC_BOB}) {
            for (String prefix : new String[]{"x/", "t/", "P/", "status/", "w/"}) {
                assertParseFailure(parser, VALID_FIELDS.replace(field, field + " " + prefix + "foo"),
                        "Unsupported prefix '" + prefix + "' for add.");
            }
            assertParseFailure(parser, VALID_FIELDS.replace(field, field + " x/"),
                    "Unsupported prefix 'x/' for add.");
            assertParseFailure(parser, VALID_FIELDS.replace(field, field + " x/2 t/VIP"),
                    "Unsupported prefix 'x/' for add.");
        }
    }

    @Test
    public void parse_unsupportedPrefixAfterWhitespace_namesPrefix() {
        for (String field : new String[]{NAME_DESC_BOB, PHONE_DESC_BOB, EMAIL_DESC_BOB, POSTAL_CODE_DESC_BOB}) {
            for (String separator : new String[]{"\t", "\n", "\r\n", " \t "}) {
                assertParseFailure(parser, VALID_FIELDS.replace(field, field + separator + "x/foo"),
                        "Unsupported prefix 'x/' for add.");
            }
        }
    }

    @Test
    public void parse_invalidValuesWithoutUnsupportedPrefixes_keepsConstraintMessages() {
        for (String suffix : new String[]{"x/foo", " x /foo", " #04/08", " https://example.com/path"}) {
            assertParseFailure(parser, VALID_FIELDS.replace(NAME_DESC_BOB, " n/Bob" + suffix),
                    Name.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, VALID_FIELDS.replace(PHONE_DESC_BOB, PHONE_DESC_BOB + suffix),
                    Phone.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, VALID_FIELDS.replace(EMAIL_DESC_BOB, EMAIL_DESC_BOB + suffix),
                    Email.MESSAGE_CONSTRAINTS);
            assertParseFailure(parser, VALID_FIELDS.replace(POSTAL_CODE_DESC_BOB, POSTAL_CODE_DESC_BOB + suffix),
                    PostalCode.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, VALID_FIELDS.replace(NAME_DESC_BOB, " n/x/foo"), Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_FIELDS.replace(PHONE_DESC_BOB, " p/x/foo"), Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_FIELDS.replace(EMAIL_DESC_BOB, " e/x/foo"), Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_FIELDS.replace(POSTAL_CODE_DESC_BOB, " c/x/foo"),
                PostalCode.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_addressWithPrefixLikeTextAndOtherFields_preservesEveryValue() {
        String address = "Block 123 c/o Alice w/o lift x/foo #04/08";
        Person expectedPerson = new PersonBuilder(BOB).withPostalCode("654321").withAddress(address).withTags().build();
        assertParseSuccess(parser, " a/" + address + POSTAL_CODE_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + NAME_DESC_BOB, new AddCommand(expectedPerson));
    }

    @Test
    public void parse_invalidAddressWithPrefixLikeText_keepsConstraintMessage() {
        assertParseFailure(parser, VALID_FIELDS.replace(ADDRESS_DESC_BOB, " a/Block 123\nx/foo"),
                Address.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unsupportedPrefixWithEarlierErrors_preservesValidationOrder() {
        String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "x/foo " + VALID_FIELDS, formatError);
        assertParseFailure(parser, VALID_FIELDS.replace(NAME_DESC_BOB, "") + " x/foo", formatError);
        assertParseFailure(parser, VALID_FIELDS.replace(PHONE_DESC_BOB, PHONE_DESC_BOB + " x/foo") + PHONE_DESC_AMY,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, VALID_FIELDS.replace(NAME_DESC_BOB, INVALID_NAME_DESC)
                .replace(PHONE_DESC_BOB, PHONE_DESC_BOB + " x/foo"), Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_FIELDS.replace(PHONE_DESC_BOB, INVALID_PHONE_DESC)
                .replace(EMAIL_DESC_BOB, EMAIL_DESC_BOB + " x/foo"), Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_FIELDS.replace(EMAIL_DESC_BOB, INVALID_EMAIL_DESC) + " x/foo",
                Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, VALID_FIELDS.replace(ADDRESS_DESC_BOB, INVALID_ADDRESS_DESC) + " x/foo",
                Address.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE);

        // missing name prefix
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + POSTAL_CODE_DESC_BOB,
                expectedMessage);

        // missing phone prefix
        assertParseFailure(parser, NAME_DESC_BOB + VALID_PHONE_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + POSTAL_CODE_DESC_BOB,
                expectedMessage);

        // missing email prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB + ADDRESS_DESC_BOB
                + POSTAL_CODE_DESC_BOB,
                expectedMessage);

        // missing address prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + VALID_ADDRESS_BOB
                + POSTAL_CODE_DESC_BOB,
                expectedMessage);

        // missing postal code prefix
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB,
                expectedMessage);

        // all prefixes missing
        assertParseFailure(parser, VALID_NAME_BOB + VALID_PHONE_BOB + VALID_EMAIL_BOB + VALID_ADDRESS_BOB,
                expectedMessage);
    }

    @Test
    public void parse_invalidValue_failure() {
        // invalid name
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + POSTAL_CODE_DESC_BOB,
                Name.MESSAGE_CONSTRAINTS);

        // invalid phone
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                + POSTAL_CODE_DESC_BOB,
                Phone.MESSAGE_CONSTRAINTS);

        // invalid email
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC + ADDRESS_DESC_BOB
                + POSTAL_CODE_DESC_BOB,
                Email.MESSAGE_CONSTRAINTS);

        // invalid address
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC
                + POSTAL_CODE_DESC_BOB,
                Address.MESSAGE_CONSTRAINTS);

        // Search prefixes must not be accepted as complete customer postal codes.
        for (String postalCode : new String[]{"5", "53", "530", "5301", "53012", "1234567"}) {
            assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + ADDRESS_DESC_BOB
                    + " c/" + postalCode, PostalCode.MESSAGE_CONSTRAINTS);
        }

        // two invalid values, only first invalid value reported
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB + INVALID_ADDRESS_DESC
                + POSTAL_CODE_DESC_BOB,
                Name.MESSAGE_CONSTRAINTS);

        // non-empty preamble
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + ADDRESS_DESC_BOB + POSTAL_CODE_DESC_BOB,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    private static String createEmailWithLength(int length) {
        String prefix = "a".repeat(64) + "@" + "b".repeat(63) + "." + "c".repeat(63) + ".";
        return prefix + "d".repeat(length - prefix.length());
    }
}
