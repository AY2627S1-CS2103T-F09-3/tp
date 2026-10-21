package seedu.address.model.search;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Tests whether a customer has the complete requested tag, ignoring case.
 */
public final class TagCriterion implements PersonCriterion {
    private final Tag tag;

    /**
     * Creates a criterion for an already validated {@code tag}.
     *
     * @throws NullPointerException If {@code tag} is null.
     */
    public TagCriterion(Tag tag) {
        this.tag = requireNonNull(tag);
    }

    @Override
    public boolean test(Person person) {
        requireNonNull(person);
        return person.getTags().contains(tag);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof TagCriterion otherCriterion)) {
            return false;
        }

        return tag.equals(otherCriterion.tag);
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("tag", tag).toString();
    }
}
