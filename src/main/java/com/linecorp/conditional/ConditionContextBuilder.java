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

import java.util.HashMap;
import java.util.Map;

public final class ConditionContextBuilder {

    private final Map<String, Object> contextVariables;

    ConditionContextBuilder() {
        contextVariables = new HashMap<>();
    }

    /**
     * Returns the {@link ConditionContextBuilder} with key-value pair set.
     *
     * @throws NullPointerException if the {@code key} is null.
     * @throws NullPointerException if the {@code value} is null.
     */
    public ConditionContextBuilder with(String key, Object value) {
        requireNonNull(key, "key");
        requireNonNull(value, "value");
        contextVariables.put(key, value);
        return this;
    }

    /**
     * Returns the {@link ConditionContextBuilder} with {@code contextVariables} set.
     *
     * @throws NullPointerException if the {@code contextVariables} is null.
     */
    public ConditionContextBuilder with(Map<String, Object> contextVariables) {
        requireNonNull(contextVariables, "contextVariables");
        if (!contextVariables.isEmpty()) {
            this.contextVariables.putAll(contextVariables);
        }
        return this;
    }

    /**
     * Returns a newly created {@link ConditionContext} by {@link ConditionContextBuilder}.
     */
    public ConditionContext build() {
        return new ConditionContext(contextVariables);
    }
}
