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

package com.linecorp.conditional;

import static com.linecorp.conditional.Condition.falseCondition;
import static com.linecorp.conditional.Condition.trueCondition;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ConditionComposerTest {

    static Stream<Arguments> complexCondition() {
        return Stream.of(
                Arguments.of(
                        // (true or false) and (true and true) = true
                        Condition.composer(ConditionOperator.AND)
                                 .with(Condition.composer(ConditionOperator.OR)
                                                .with(trueCondition(), falseCondition())
                                                .compose(),
                                       Condition.composer(ConditionOperator.AND)
                                                .with(trueCondition(), trueCondition())
                                                .compose()).compose(),
                        trueCondition().or(falseCondition()).and(trueCondition().and(trueCondition())),
                        true),
                Arguments.of(
                        // (true or false) and true = true
                        Condition.composer(ConditionOperator.AND)
                                 .with(Condition.composer(ConditionOperator.OR)
                                                .with(trueCondition(), falseCondition())
                                                .compose(),
                                       trueCondition()).compose(),
                        trueCondition().or(falseCondition()).and(trueCondition()),
                        true),
                Arguments.of(
                        // true and (true or false) = true
                        Condition.composer(ConditionOperator.AND)
                                 .with(trueCondition(),
                                       Condition.composer(ConditionOperator.OR)
                                                .with(trueCondition(), falseCondition())
                                                .compose()).compose(),
                        trueCondition().and(trueCondition().or(falseCondition())),
                        true),
                Arguments.of(
                        // ((true or false) and true) or false = true
                        Condition.composer(ConditionOperator.OR)
                                 .with(Condition.composer(ConditionOperator.AND)
                                                .with(Condition.composer(ConditionOperator.OR)
                                                               .with(trueCondition(), falseCondition())
                                                               .compose(),
                                                      trueCondition()).compose(), falseCondition()).compose(),
                        trueCondition().or(falseCondition()).and(trueCondition())
                                       .or(falseCondition()),
                        true),
                Arguments.of(
                        // true and (false or (true and false)) = false
                        Condition.composer(ConditionOperator.AND)
                                 .with(trueCondition(),
                                       Condition.composer(ConditionOperator.OR)
                                                .with(falseCondition(),
                                                      Condition.composer(ConditionOperator.AND)
                                                               .with(trueCondition(),
                                                                     falseCondition()).compose())
                                                .compose()).compose(),
                        trueCondition().and(falseCondition().or(trueCondition().and(falseCondition()))),
                        false)
        );
    }

    @ParameterizedTest
    @MethodSource("complexCondition")
    void complex(Condition hardWay, Condition easyWay, boolean expectedMatches) {
        assertThat(hardWay.matches(ConditionContext.of())).isEqualTo(expectedMatches);
        assertThat(easyWay.matches(ConditionContext.of())).isEqualTo(expectedMatches);
    }
}
