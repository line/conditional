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

import static java.util.Objects.requireNonNull;

import java.util.List;

final class ComposedConditionAttributeMutator extends ConditionAttributeMutator {

    private final Operator operator;
    private volatile List<Condition> conditions;

    ComposedConditionAttributeMutator(ComposedCondition composedCondition) {
        super(composedCondition);
        operator = composedCondition.operator();
        conditions = composedCondition.conditions();
    }

    ComposedConditionAttributeMutator conditions(List<Condition> conditions) {
        this.conditions = requireNonNull(conditions, "conditions");
        return this;
    }

    @Override
    ComposedCondition mutate() {
        return new ComposedCondition(function, alias, async, executor, delayMillis, timeoutMillis,
                                     operator, conditions);
    }
}
