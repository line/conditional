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
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

public final class ConditionContext {

    private final List<ConditionMatchResult> logs = new CopyOnWriteArrayList<>();
    private final Map<String, Object> contextVariables;

    ConditionContext(Map<String, Object> contextVariables) {
        this.contextVariables = requireNonNull(contextVariables, "contextVariables");
    }

    /**
     * Returns a newly created {@link ConditionContext}.
     */
    public static ConditionContext of() {
        return of(Map.of());
    }

    /**
     * Returns a newly created {@link ConditionContext} by {@code contextVariables}.
     */
    public static ConditionContext of(Map<String, Object> contextVariables) {
        return new ConditionContext(Map.copyOf(contextVariables));
    }

    /**
     * Returns a newly created {@link ConditionContext} by key-value pair.
     */
    public static ConditionContext of(String k1, Object v1) {
        return of(Map.of(k1, v1));
    }

    /**
     * Returns a newly created {@link ConditionContext} by key-value pairs.
     */
    public static ConditionContext of(String k1, Object v1, String k2, Object v2) {
        return of(Map.of(k1, v1, k2, v2));
    }

    /**
     * Returns a newly created {@link ConditionContext} by key-value pairs.
     */
    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3) {
        return of(Map.of(k1, v1, k2, v2, k3, v3));
    }

    /**
     * Returns a newly created {@link ConditionContext} by key-value pairs.
     */
    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    /**
     * Returns a newly created {@link ConditionContext} by key-value pairs.
     */
    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
    }

    /**
     * Returns a newly created {@link ConditionContext} by key-value pairs.
     */
    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5, String k6, Object v6) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6));
    }

    /**
     * Returns a newly created {@link ConditionContext} by key-value pairs.
     */
    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5, String k6, Object v6,
                                      String k7, Object v7) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7));
    }

    /**
     * Returns a newly created {@link ConditionContext} by key-value pairs.
     */
    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5, String k6, Object v6,
                                      String k7, Object v7, String k8, Object v8) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8));
    }

    /**
     * Returns a newly created {@link ConditionContext} by key-value pairs.
     */
    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5, String k6, Object v6,
                                      String k7, Object v7, String k8, Object v8,
                                      String k9, Object v9) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9));
    }

    /**
     * Returns a newly created {@link ConditionContext} by key-value pairs.
     */
    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5, String k6, Object v6,
                                      String k7, Object v7, String k8, Object v8,
                                      String k9, Object v9, String k10, Object v10) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
    }

    /**
     * Returns the {@link ConditionContextBuilder}.
     */
    public static ConditionContextBuilder builder() {
        return new ConditionContextBuilder();
    }

    /**
     * Returns the match logs of {@link Condition}.
     */
    public List<ConditionMatchResult> logs() {
        return List.copyOf(logs);
    }

    /**
     * Returns the value corresponding to the key from {@code contextVariables}.
     *
     * @param key the {@code key} to get {@code value} from {@code contextVariables}.
     *
     * @return null if the {@code value} corresponding to the {@code key} does not exist.
     *
     * @throws NullPointerException if the {@code key} is null.
     */
    @Nullable
    public Object var(String key) {
        return contextVariables.get(key);
    }

    /**
     * Returns the value corresponding to the key from {@code contextVariables} with type casting.
     *
     * @param key the {@code key} to get {@code value} from {@code contextVariables}.
     * @param as the type to cast the {@code value}.
     *
     * @return null if the {@code value} corresponding to the {@code key} does not exist, or it cannot be cast to {@code as} type.
     *
     * @throws NullPointerException if {@code key} or {@code as} is null.
     */
    @Nullable
    public <T> T var(String key, Class<T> as) {
        return castOrNull(var(key), as);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> T castOrNull(@Nullable Object var, Class<T> as) {
        requireNonNull(as, "as");
        if (!as.isInstance(var)) {
            return null;
        }
        return (T) var;
    }

    /**
     * Returns the value corresponding to the key from {@code contextVariables} with type casting.
     *
     * @param key the {@code key} to get {@code value} from {@code contextVariables}.
     *
     * @throws NullPointerException if {@code key} or {@code value} corresponding to the {@code key} is null.
     */
    public Object mustVar(String key) {
        return must(contextVariables.get(key));
    }

    /**
     * Returns the value corresponding to the key from {@code contextVariables} with type casting.
     *
     * @param key the {@code key} to get {@code value} from {@code contextVariables}.
     * @param as the type to cast the {@code value}.
     *
     * @throws NullPointerException if {@code key} or {@code value} corresponding to the {@code key} or {@code as} is null.
     * @throws ClassCastException if the {@code value} cannot be cast to {@code as} type.
     */
    public <T> T mustVar(String key, Class<T> as) {
        return castOrThrow(mustVar(key), as);
    }

    private static <VAR> VAR must(VAR var) {
        return requireNonNull(var, "var");
    }

    @SuppressWarnings("unchecked")
    private static <T> T castOrThrow(Object var, Class<T> as) {
        requireNonNull(var, "var");
        requireNonNull(as, "as");
        if (!as.isInstance(var)) {
            throw new ClassCastException("'" + var + "' cannot be cast to " + as.getName() +
                                         " (actual type is " + var.getClass().getName() + ')');
        }
        return (T) var;
    }

    /**
     * Returns a newly created {@link ConditionContext} from existing {@link ConditionContext}.
     * {@code contextVariables} is copied from the existing {@link ConditionContext}, but {@code logs} is not.
     * This is useful when invoking {@link Condition#matches(ConditionContext)} related methods after initializing {@code logs}.
     */
    public ConditionContext copy() {
        return of(contextVariables);
    }

    void completed(Thread thread, Condition condition, boolean matches,
                   long startTimeMillis, long endTimeMillis) {
        logs.add(ConditionMatchResult.completed(thread, condition, matches, startTimeMillis, endTimeMillis));
    }

    void failed(Thread thread, Condition condition, Throwable cause,
                long startTimeMillis, long endTimeMillis) {
        logs.add(ConditionMatchResult.failed(thread, condition, cause, startTimeMillis, endTimeMillis));
    }

    void cancelled(Thread thread, Condition condition, Throwable cause,
                   long startTimeMillis, long endTimeMillis) {
        logs.add(ConditionMatchResult.cancelled(thread, condition, cause, startTimeMillis, endTimeMillis));
    }

    void timedOut(Thread thread, Condition condition, Throwable cause,
                  long startTimeMillis, long endTimeMillis) {
        logs.add(ConditionMatchResult.timedOut(thread, condition, cause, startTimeMillis, endTimeMillis));
    }
}
