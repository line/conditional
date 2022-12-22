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

import java.util.ArrayList;
import java.util.List;

public final class ConditionComposer {

    private final Operator operator;
    private final List<Condition> conditions = new ArrayList<>();

    ConditionComposer(Operator operator) {
        this.operator = requireNonNull(operator, "operator");
    }

    /**
     * Returns the {@link ConditionComposer} composed by {@code conditions}.
     *
     * @param conditions the {@code conditions} to compose.
     */
    public ConditionComposer with(Condition... conditions) {
        return with(List.of(conditions));
    }

    /**
     * Returns the {@link ConditionComposer} composed by {@code conditions}.
     *
     * @param conditions the {@code conditions} to compose.
     *
     * @throws NullPointerException if the {@code conditions} is null.
     */
    public ConditionComposer with(List<Condition> conditions) {
        requireNonNull(conditions, "conditions");
        if (!conditions.isEmpty()) {
            this.conditions.addAll(conditions);
        }
        return this;
    }

    /**
     * Returns a newly created {@link ComposedCondition} by {@link ConditionComposer}.
     */
    public ComposedCondition compose() {
        return new ComposedCondition(operator, conditions);
    }
}
