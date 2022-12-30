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
