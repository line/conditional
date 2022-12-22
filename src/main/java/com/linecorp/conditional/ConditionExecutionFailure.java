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

public final class ConditionExecutionFailure extends ConditionExecutionResult {

    private final Throwable cause;

    ConditionExecutionFailure(Thread thread, Condition condition,
                              Throwable cause, long durationMillis) {
        super(thread, condition, durationMillis);
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
        return "ConditionExecutionFailure{" +
               "thread=" + thread().getName() +
               ", condition=" + condition() +
               ", cause=" + cause +
               ", duration=" + durationMillis() + "ms" +
               ", timeout=" + timeoutAsString() +
               '}';
    }
}
