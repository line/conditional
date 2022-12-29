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

public final class ConditionMatchFailure extends ConditionMatchResult {

    private final Throwable cause;

    ConditionMatchFailure(Thread thread, Condition condition,
                          Throwable cause, long startTimeMillis, long endTimeMillis) {
        super(thread, condition, startTimeMillis, endTimeMillis);
        this.cause = requireNonNull(cause, "cause");
    }

    /**
     * Returns the {@code cause}.
     */
    public Throwable cause() {
        return cause;
    }

    @Override
    public String toString() {
        return "ConditionMatchFailure{" +
               "condition=" + condition() +
               ", cause=" + cause +
               ", async=" + condition().isAsync() +
               ", thread=" + thread().getName() +
               ", delay=" + millisAsString(condition().delayMillis()) +
               ", timeout=" + millisAsString(condition().timeoutMillis()) +
               ", startTime=" + millisAsString(startTimeMillis()) +
               ", endTime=" + millisAsString(endTimeMillis()) +
               ", duration=" + millisAsString(durationMillis()) +
               '}';
    }
}