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

public abstract class ConditionExecutionResult {

    private final Thread thread;
    private final Condition condition;
    private final long startTimeMillis;
    private final long endTimeMillis;
    private final long durationMillis;

    protected ConditionExecutionResult(Thread thread, Condition condition,
                                       long startTimeMillis, long endTimeMillis) {
        requireNonNull(thread, "thread");
        requireNonNull(condition, "condition");
        if (startTimeMillis > endTimeMillis) {
            throw new IllegalArgumentException("startTimeMillis > endTimeMillis (expected <= endTimeMillis)");
        }
        this.thread = thread;
        this.condition = condition;
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
        durationMillis = endTimeMillis - startTimeMillis;
    }

    /**
     * Returns the {@code thread}.
     */
    public final Thread thread() {
        return thread;
    }

    /**
     * Returns the {@code condition}.
     */
    public final Condition condition() {
        return condition;
    }

    /**
     * Returns the {@code startTimeMillis}.
     */
    public final long startTimeMillis() {
        return startTimeMillis;
    }

    /**
     * Returns the {@code endTimeMillis}.
     */
    public final long endTimeMillis() {
        return endTimeMillis;
    }

    /**
     * Returns the {@code durationMillis}.
     */
    public final long durationMillis() {
        return durationMillis;
    }

    protected static String millisAsString(long millis) {
        return millis == Long.MAX_VALUE ? "INF" : millis + "ms";
    }

    @Override
    public abstract String toString();
}
