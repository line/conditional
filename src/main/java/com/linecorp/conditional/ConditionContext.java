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

    private final List<ConditionExecutionResult> conditionExecutionResults = new CopyOnWriteArrayList<>();
    private final Map<String, Object> contextVariables;

    ConditionContext(Map<String, Object> contextVariables) {
        this.contextVariables = requireNonNull(contextVariables, "contextVariables");
    }

    public static ConditionContext of() {
        return of(Map.of());
    }

    public static ConditionContext of(Map<String, Object> contextVariables) {
        return new ConditionContext(Map.copyOf(contextVariables));
    }

    public static ConditionContext of(String k1, Object v1) {
        return of(Map.of(k1, v1));
    }

    public static ConditionContext of(String k1, Object v1, String k2, Object v2) {
        return of(Map.of(k1, v1, k2, v2));
    }

    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3) {
        return of(Map.of(k1, v1, k2, v2, k3, v3));
    }

    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4));
    }

    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5));
    }

    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5, String k6, Object v6) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6));
    }

    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5, String k6, Object v6,
                                      String k7, Object v7) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7));
    }

    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5, String k6, Object v6,
                                      String k7, Object v7, String k8, Object v8) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8));
    }

    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5, String k6, Object v6,
                                      String k7, Object v7, String k8, Object v8,
                                      String k9, Object v9) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9));
    }

    public static ConditionContext of(String k1, Object v1, String k2, Object v2,
                                      String k3, Object v3, String k4, Object v4,
                                      String k5, Object v5, String k6, Object v6,
                                      String k7, Object v7, String k8, Object v8,
                                      String k9, Object v9, String k10, Object v10) {
        return of(Map.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10));
    }

    public static ConditionContextBuilder builder() {
        return new ConditionContextBuilder();
    }

    public List<ConditionExecutionResult> conditionExecutionResults() {
        return conditionExecutionResults;
    }

    public Object var(String key) {
        return must(contextVariables.get(key));
    }

    public <T> T var(String key, Class<T> as) {
        return castOrThrow(var(key), as);
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

    @Nullable
    public Object safeVar(String key) {
        return contextVariables.get(key);
    }

    @Nullable
    public <T> T safeVar(String key, Class<T> as) {
        return castOrNull(safeVar(key), as);
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

    void addConditionExecutionResult(Thread thread, Condition condition,
                                     boolean matches, long durationMillis) {
        conditionExecutionResults.add(new ConditionExecutionCompletion(thread, condition,
                                                                       matches, durationMillis));
    }

    void addConditionExecutionResult(Thread thread, Condition condition,
                                     Throwable cause, long durationMillis) {
        conditionExecutionResults.add(new ConditionExecutionFailure(thread, condition, cause, durationMillis));
    }
}
