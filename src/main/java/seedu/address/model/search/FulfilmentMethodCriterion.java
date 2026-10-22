package seedu.address.model.search;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.person.Person;
import seedu.address.model.preorder.FulfilmentMethod;
import seedu.address.model.preorder.Preorder;

/** Matches the shared preorder method, including on preorders without items. */
public final class FulfilmentMethodCriterion implements PersonCriterion {
    private final FulfilmentMethod method;

    public FulfilmentMethodCriterion(FulfilmentMethod method) {
        this.method = requireNonNull(method);
    }

    @Override
    public boolean test(Person person) {
        return requireNonNull(person).getPreorder().flatMap(Preorder::getMethod).filter(method::equals).isPresent();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FulfilmentMethodCriterion criterion && method == criterion.method;
    }

    @Override
    public int hashCode() {
        return method.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("method", method).toString();
    }
}
