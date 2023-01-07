/*
 * Copyright 2022 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.conditional.kotlin

import com.linecorp.conditional.Condition
import com.linecorp.conditional.ConditionContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class ConditionExtensionNotConditionTest {

    @Test
    fun operator_NOT() {
        assertThat(!trueCondition.matches(ConditionContext.of())).isFalse
        assertThat(!falseCondition.matches(ConditionContext.of())).isTrue
        assertThatThrownBy { !failed.matches(ConditionContext.of()) }
            .isExactlyInstanceOf(RuntimeException::class.java)
    }

    private companion object {
        private val trueCondition = Condition.trueCondition()
        private val falseCondition = Condition.falseCondition()
        private val failed = Condition.failed { _: ConditionContext -> RuntimeException() }
    }
}
