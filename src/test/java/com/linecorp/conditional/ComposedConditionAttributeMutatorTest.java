package com.linecorp.conditional;

import static com.linecorp.conditional.Condition.falseCondition;
import static com.linecorp.conditional.Condition.trueCondition;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ComposedConditionAttributeMutatorTest {

    @Test
    void conditions() {
        final var mutator = new ComposedConditionAttributeMutator(trueCondition().or(falseCondition()));
        final var conditions = List.of(trueCondition(), trueCondition());
        assertThat(mutator.conditions()).isNotEqualTo(conditions);
        mutator.conditions(conditions);
        assertThat(mutator.mutate().conditions()).isEqualTo(conditions);
    }

    @Test
    void cancellable() {
        final var mutator = new ComposedConditionAttributeMutator(trueCondition().or(falseCondition()));
        final var cancellable = true;
        assertThat(mutator.cancellable()).isNotEqualTo(cancellable);
        mutator.cancellable(cancellable);
        assertThat(mutator.mutate().cancellable()).isEqualTo(cancellable);
    }
}
