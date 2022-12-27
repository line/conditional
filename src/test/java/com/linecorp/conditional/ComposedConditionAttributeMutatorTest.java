package com.linecorp.conditional;

import static com.linecorp.conditional.Condition.falseCondition;
import static com.linecorp.conditional.Condition.trueCondition;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class ComposedConditionAttributeMutatorTest {

    @Test
    void conditions() {
        final var mutator = new ComposedConditionAttributeMutator(trueCondition().or(falseCondition()));
        final var conditions = List.of(trueCondition(), trueCondition());
        assertNotEquals(conditions, mutator.conditions());
        mutator.conditions(conditions);
        assertEquals(conditions, mutator.mutate().conditions());
    }

    @Test
    void cancellable() {
        final var mutator = new ComposedConditionAttributeMutator(trueCondition().or(falseCondition()));
        final var cancellable = true;
        assertNotEquals(cancellable, mutator.cancellable());
        mutator.cancellable(cancellable);
        assertEquals(cancellable, mutator.mutate().cancellable());
    }
}