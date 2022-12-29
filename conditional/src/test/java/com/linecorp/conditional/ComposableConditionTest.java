package com.linecorp.conditional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.Test;

class ComposableConditionTest {

    @Test
    void matches() {
        final var condition = new ComposableCondition() {

            @Override
            protected Condition compose() {
                final var a = Condition.async(ctx -> true).alias("a");
                final var b = Condition.failed(new RuntimeException()).async().delay(1000).alias("b");
                return a.and(b);
            }
        }.alias("Composed");
        final var ctx = ConditionContext.of();
        assertThatThrownBy(() -> condition.matches(ctx))
                .isExactlyInstanceOf(CompletionException.class)
                .hasCauseInstanceOf(RuntimeException.class);
        assertThat(ctx.logs().size()).isEqualTo(4);
    }
}
