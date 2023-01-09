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

import java.time.Instant;
import java.time.ZoneId;

import javax.annotation.Nullable;

public final class ConditionMatchResult {

    private final Thread thread;
    private final Condition condition;
    private final ConditionMatchState state;
    @Nullable
    private final Boolean matches;
    @Nullable
    private final Throwable cause;
    private final long startTimeMillis;
    private final long endTimeMillis;
    private final long durationMillis;

    private ConditionMatchResult(Thread thread, Condition condition, ConditionMatchState state,
                                 @Nullable Boolean matches, @Nullable Throwable cause,
                                 long startTimeMillis, long endTimeMillis) {
        requireNonNull(thread, "thread");
        requireNonNull(condition, "condition");
        requireNonNull(state, "state");
        switch (state) {
            case COMPLETED -> requireNonNull(matches, "matches");
            case FAILED, CANCELLED, TIMED_OUT -> requireNonNull(cause, "cause");
        }
        if (startTimeMillis > endTimeMillis) {
            throw new IllegalArgumentException("startTimeMillis > endTimeMillis (expected <= endTimeMillis)");
        }
        this.thread = thread;
        this.condition = condition;
        this.state = state;
        this.matches = matches;
        this.cause = cause;
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
        durationMillis = endTimeMillis - startTimeMillis;
    }

    static ConditionMatchResult completed(Thread thread, Condition condition, boolean matches,
                                          long startTimeMillis, long endTimeMillis) {
        return new ConditionMatchResult(thread, condition, ConditionMatchState.COMPLETED,
                                        matches, null, startTimeMillis, endTimeMillis);
    }

    static ConditionMatchResult failed(Thread thread, Condition condition, Throwable cause,
                                       long startTimeMillis, long endTimeMillis) {
        return new ConditionMatchResult(thread, condition, ConditionMatchState.FAILED,
                                        null, cause, startTimeMillis, endTimeMillis);
    }

    static ConditionMatchResult cancelled(Thread thread, Condition condition, Throwable cause,
                                          long startTimeMillis, long endTimeMillis) {
        return new ConditionMatchResult(thread, condition, ConditionMatchState.CANCELLED,
                                        null, cause, startTimeMillis, endTimeMillis);
    }

    static ConditionMatchResult timedOut(Thread thread, Condition condition, Throwable cause,
                                         long startTimeMillis, long endTimeMillis) {
        return new ConditionMatchResult(thread, condition, ConditionMatchState.TIMED_OUT,
                                        null, cause, startTimeMillis, endTimeMillis);
    }

    /**
     * Returns the {@code thread}.
     */
    public Thread thread() {
        return thread;
    }

    /**
     * Returns the {@code condition}.
     */
    public Condition condition() {
        return condition;
    }

    /**
     * Returns the {@code state}.
     */
    public ConditionMatchState state() {
        return state;
    }

    /**
     * Returns whether the match result is {@code ConditionMatchState.COMPLETED}.
     */
    public boolean completed() {
        return state == ConditionMatchState.COMPLETED;
    }

    /**
     * Returns whether the match result is {@code ConditionMatchState.FAILED}.
     */
    public boolean failed() {
        return state == ConditionMatchState.FAILED;
    }

    /**
     * Returns whether the match result is {@code ConditionMatchState.CANCELLED}.
     */
    public boolean cancelled() {
        return state == ConditionMatchState.CANCELLED;
    }

    /**
     * Returns whether the match result is {@code ConditionMatchState.TIMED_OUT}.
     */
    public boolean timedOut() {
        return state == ConditionMatchState.TIMED_OUT;
    }

    /**
     * Returns the {@code matches}.
     */
    @Nullable
    public Boolean matches() {
        return matches;
    }

    /**
     * Returns the {@code cause}.
     */
    @Nullable
    public Throwable cause() {
        return cause;
    }

    /**
     * Returns the {@code startTimeMillis}.
     */
    public long startTimeMillis() {
        return startTimeMillis;
    }

    /**
     * Returns the {@code endTimeMillis}.
     */
    public long endTimeMillis() {
        return endTimeMillis;
    }

    /**
     * Returns the {@code durationMillis}.
     */
    public long durationMillis() {
        return durationMillis;
    }

    private static String millisAsString(long millis) {
        return millis == Long.MAX_VALUE ? "INF" : millis + "ms";
    }

    private static String millisAsISO8601String(long millis) {
        return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toOffsetDateTime().toString();
    }

    @Override
    public String toString() {
        final var builder = new StringBuilder();
        builder.append("ConditionMatchResult{")
               .append("condition=").append(condition)
               .append(", state=").append(state);
        switch (state) {
            case COMPLETED -> builder.append(", matches=").append(matches);
            case FAILED, CANCELLED, TIMED_OUT -> builder.append(", cause=").append(cause);
        }
        builder.append(", async=").append(condition.isAsync())
               .append(", thread=").append(thread.getName())
               .append(", delay=").append(millisAsString(condition.delayMillis()))
               .append(", timeout=").append(millisAsString(condition.timeoutMillis()))
               .append(", startTime=").append(millisAsISO8601String(startTimeMillis))
               .append(", endTime=").append(millisAsISO8601String(endTimeMillis))
               .append(", duration=").append(millisAsString(durationMillis))
               .append('}');
        return builder.toString();
    }
}
