package com.linecorp.conditional;

import static com.linecorp.conditional.Condition.falseCondition;
import static com.linecorp.conditional.Condition.trueCondition;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ComposedConditionAttributeUpdaterTest {

    @Test
    void conditions() {
        final var attributeUpdater =
                new ComposedConditionAttributeUpdater(trueCondition().or(falseCondition()));
        final var conditions = List.of(trueCondition(), trueCondition());
        assertThat(attributeUpdater.conditions()).isNotEqualTo(conditions);
        attributeUpdater.conditions(conditions);
        assertThat(attributeUpdater.update().conditions()).isEqualTo(conditions);
    }

    @Test
    void cancellable() {
        final var attributeUpdater =
                new ComposedConditionAttributeUpdater(trueCondition().or(falseCondition()));
        final var cancellable = true;
        assertThat(attributeUpdater.cancellable()).isNotEqualTo(cancellable);
        attributeUpdater.cancellable(cancellable);
        assertThat(attributeUpdater.update().cancellable()).isEqualTo(cancellable);
    }
}
