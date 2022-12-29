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

public final class ConditionMatchCompletion extends ConditionMatchResult {

    private final boolean matches;

    ConditionMatchCompletion(Thread thread, Condition condition,
                             boolean matches, long startTimeMillis, long endTimeMillis) {
        super(thread, condition, startTimeMillis, endTimeMillis);
        this.matches = matches;
    }

    /**
     * Returns the {@code matches}.
     */
    public boolean matches() {
        return matches;
    }

    @Override
    public String toString() {
        return "ConditionMatchCompletion{" +
               "condition=" + condition() +
               ", matches=" + matches +
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