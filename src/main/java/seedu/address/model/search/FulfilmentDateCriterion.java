package seedu.address.model.search;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentDate;
import seedu.address.model.preorder.Preorder;

/** Matches the shared preorder date, including on preorders without items. */
public final class FulfilmentDateCriterion implements PersonCriterion {
    private final FulfilmentDate date;

    public FulfilmentDateCriterion(FulfilmentDate date) {
        this.date = requireNonNull(date);
    }

    @Override
    public boolean test(Person person) {
        return requireNonNull(person).getPreorder().flatMap(Preorder::getDate).filter(date::equals).isPresent();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FulfilmentDateCriterion criterion && date.equals(criterion.date);
    }

    @Override
    public int hashCode() {
        return date.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("date", date).toString();
    }
}
