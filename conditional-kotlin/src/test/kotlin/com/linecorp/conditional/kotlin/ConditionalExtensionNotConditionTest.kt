package com.linecorp.conditional.kotlin

import com.linecorp.conditional.Condition
import com.linecorp.conditional.ConditionContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class ConditionalExtensionNotConditionTest {

    @Test
    fun operator_NOT() {
        assertThat(!trueCondition.matches(ConditionContext.of())).isFalse
        assertThat(!falseCondition.matches(ConditionContext.of())).isTrue
        assertThatThrownBy { !failed.matches(ConditionContext.of()) }
            .isExactlyInstanceOf(RuntimeException::class.java)
    }

    companion object {
        private val trueCondition = Condition.trueCondition()
        private val falseCondition = Condition.falseCondition()
        private val failed = Condition.failed { _: ConditionContext -> RuntimeException() }
    }
}
