package seedu.address.testutil;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.PostalCode;
import seedu.address.model.preorder.Item;
import seedu.address.model.preorder.ItemStatus;
import seedu.address.model.preorder.Preorder;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_ADDRESS = "123, Jurong West Ave 6, #08-111";

    private Name name;
    private Phone phone;
    private Email email;
    private Address address;
    private PostalCode postalCode;
    private Set<Tag> tags;
    private Optional<Preorder> preorder;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        address = new Address(DEFAULT_ADDRESS);
        postalCode = null;
        tags = new HashSet<>();
        preorder = Optional.empty();
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        address = personToCopy.getAddress();
        postalCode = personToCopy.getPostalCode();
        tags = new HashSet<>(personToCopy.getTags());
        preorder = personToCopy.getPreorder();
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code Person} that we are building.
     */
    public PersonBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the postal code of the {@code Person} that we are building.
     */
    public PersonBuilder withPostalCode(String postalCode) {
        this.postalCode = postalCode == null ? null : new PostalCode(postalCode);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /**
     * Sets the {@code Preorder} of the {@code Person} that we are building to a single item with quantity 1.
     */
    public PersonBuilder withPreorder(String itemName, ItemStatus status) {
        return withPreorder(new ItemBuilder().withName(itemName).withQuantity("1").withStatus(status).build());
    }

    /**
     * Sets the {@code Preorder} of the {@code Person} that we are building to contain {@code items}.
     * With no arguments, the preorder has no items yet.
     */
    public PersonBuilder withPreorder(Item... items) {
        this.preorder = Optional.of(new Preorder(List.of(items)));
        return this;
    }

    public Person build() {
        return new Person(name, phone, email, address, postalCode, tags, preorder);
    }

}
