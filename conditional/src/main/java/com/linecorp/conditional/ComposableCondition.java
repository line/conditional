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

/**
 * <p>{@link ComposableCondition} is a class to compose multiple {@link Condition}s.</p>
 * <p>If you don't use {@link ComposableCondition}, you can compose like the code below:</p>
 * <pre>
 * class TestCondition extends Condition {
 *     Condition a;
 *     Condition b;
 *
 *     &#064;Override
 *     protected boolean match(ConditionContext ctx) {
 *         // You have to invoke {@link Condition#matches(ConditionContext)} manually.
 *         return a.and(b).matches(ctx);
 *     }
 * }
 * </pre>
 * <p>Otherwise, if you use {@link ComposableCondition}:</p>
 * <pre>
 * class TestCondition extends ComposableCondition {
 *     Condition a;
 *     Condition b;
 *
 *     &#064;Override
 *     protected Condition compose() {
 *         // You don't have to invoke {@link Condition#matches(ConditionContext)} manually.
 *         return a.and(b);
 *     }
 * }
 * </pre>
 */
public abstract class ComposableCondition extends Condition {

    @Override
    protected final boolean match(ConditionContext ctx) {
        return compose().matches(ctx);
    }

    protected abstract Condition compose();
}
