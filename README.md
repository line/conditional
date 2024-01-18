# Conditional

> Make your own conditional expressions more elegant.

[![build](https://github.com/line/conditional/actions/workflows/gradle.yml/badge.svg?branch=main)](https://github.com/line/conditional/actions/workflows/gradle.yml)
<a href="https://github.com/line/conditional/contributors"><img src="https://img.shields.io/github/contributors/line/conditional.svg"></a>
<a href="https://search.maven.org/search?q=g:com.linecorp.conditional%20AND%20a:conditional"><img src="https://img.shields.io/maven-central/v/com.linecorp.conditional/conditional.svg?label=version"></a>
<a href="https://github.com/line/conditional/commits"><img src="https://img.shields.io/github/release-date/line/conditional.svg?label=release"></a>
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Conditional is a super lightweight library that helps you make conditional expressions. You can compose multiple conditional expressions and make them asynchronous easily. This library is designed to simplify the creation and management of complex conditional expressions, especially in asynchronous programming scenarios.

## Why do we need Conditional?

Let's make a simple conditional expression to see how Conditional is useful. Consider the following expression:
```
(a and b) or (c and d)
```
Here, `a`, `b`, `c`, and `d` are boolean variables.
The expression evaluates to `true` if either `a` and `b` are both `true`, or `c` and `d` are both `true`.

An asynchronous implementation of this would be:
```java
CompletableFuture<Boolean> a = CompletableFuture.supplyAsync(() -> { sleep(3000); return true; });
CompletableFuture<Boolean> b = CompletableFuture.supplyAsync(() -> { sleep(1000); return false; });
CompletableFuture<Boolean> c = CompletableFuture.supplyAsync(() -> { sleep(1500); return false; });
CompletableFuture<Boolean> d = CompletableFuture.supplyAsync(() -> { sleep(2500); return true; });
CompletableFuture<Boolean> future =
        a.thenCombine(b, (ra, rb) -> ra && rb)
         .thenCombine(c.thenCombine(d, (rc, rd) -> rc && rd), (rab, rcd) -> rab || rcd);
assert future.join() == false; // 👈 It takes about 3000 milliseconds...
```

It's a simple conditional expression, but not trivial to implement asynchronously.
Let's try to use Conditional to simplify this asynchronous implementation:
```java
Condition a = Condition.of(ctx -> { sleep(3000); return true; });
Condition b = Condition.of(ctx -> { sleep(1000); return false; });
Condition c = Condition.of(ctx -> { sleep(1500); return false; });
Condition d = Condition.of(ctx -> { sleep(2500); return true; });
Condition condition = (a.and(b)).or(c.and(d));
ConditionContext ctx = ConditionContext.of();
assert condition.parallel().matches(ctx) == false; // 👈 It takes about 1500 milliseconds!
```

It's much more readable than before. Also, there is a difference in execution time.
Conditional is optimized to do less computation.
Looking at the conditional expression above, `b` and `c` are `false`.
So, regardless of the results of `a` and `d`, the result of the entire conditional expression is `false`.
In this case, Conditional does performance optimization internally for less computation, so the result can be returned faster.

As above, we can make conditional expressions more elegant. Let's dive into the Conditional.

## Getting started

To add a dependency on Conditional using Gradle, use the following:
```groovy
dependencies {
    implementation("com.linecorp.conditional:conditional:1.1.3")
}
```

To add a dependency using Maven:
```xml
<dependency>
    <groupId>com.linecorp.conditional</groupId>
    <artifactId>conditional</artifactId>
    <version>1.1.3</version>
</dependency>
```

It takes only 3 steps to make and match a conditional expression.
```java
// Step 1: Make a conditional expression.
Condition a = Condition.of(ctx -> ctx.var("a", Boolean.class)); // This condition checks the value of the variable "a" in the context.
Condition b = Condition.of(ctx -> ctx.var("b", Boolean.class)); // This condition checks the value of the variable "b" in the context.
Condition condition = a.and(b);

// Step 2: Make a context for matching conditional expression.
ConditionContext ctx = ConditionContext.of("a", true, "b", true);

// Step 3: Match a conditional expression.
assert condition.matches(ctx) == true;
```
In this example, we create two conditions, `a` and `b`, which return the Boolean values of the corresponding variables from the context.
We then combine these two conditions using the `and` operator to create a new condition.
Finally, we create a context with the variables `a` and `b` both set to `true`, and verify if the `condition.matches(ctx)` is `true`.

Frequently used conditional expressions such as `true/false/completed/failed` can be made as follows.
```java
Condition.trueCondition();
Condition.falseCondition();
Condition.completed(true);
Condition.completed(false);
Condition.failed(new RuntimeException());
Condition.failed(() -> new RuntimeException());
Condition.failed(ctx -> new RuntimeException());
```

If you want to set a timeout in a conditional expression, you can do something like this:
```java
Condition.of(ctx -> true, 3000, TimeUnit.MILLISECONDS);
Condition.of(ctx -> true).timeout(3000, TimeUnit.MILLISECONDS);
```
This code snippet demonstrates how to set a timeout for a condition.
The condition will fail if it does not complete within the specified time limit.

If timeout is exceeded, a `TimeoutException` is raised.
```java
Condition condition = Condition.of(ctx -> {
    try {
        TimeUnit.MILLISECONDS.sleep(5000);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    return true;
}).timeout(3000, TimeUnit.MILLISECONDS);
ConditionContext ctx = ConditionContext.of();
condition.matches(ctx); // 👈 TimeoutException will be raised after 3 seconds.
```

You can also set timeout for more complex conditional expression.
```java
Condition a = Condition.of(ctx -> {
    try {
        TimeUnit.MILLISECONDS.sleep(3000);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    return true;
}).timeout(3500, TimeUnit.MILLISECONDS);
Condition b = Condition.of(ctx -> {
    try {
        TimeUnit.MILLISECONDS.sleep(4500);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    return true;
}).timeout(4000, TimeUnit.MILLISECONDS);
Condition condition = a.and(b).timeout(8000, TimeUnit.MILLISECONDS);
ConditionContext ctx = ConditionContext.of();
condition.matches(ctx); // 👈 TimeoutException will be raised due to 'b' after 4 seconds.
```
In this example, we create two conditions `a` and `b`, each with their own timeout.
We then combine these conditions with an and operator and set a global timeout for the combined condition.
If either of the conditions or the entire condition does not complete within their respective timeouts, a `TimeoutException` will be raised.

## Asynchronous support

Conditional supports asynchronous for higher performance in I/O intensive operations.
Asynchronous operations allow multiple tasks to be executed concurrently, which can significantly improve the performance of your application, especially when dealing with I/O intensive tasks.

First, let's look at the simple synchronous conditional expressions.
```java
Condition a = Condition.delayed(ctx -> true, 3000, TimeUnit.MILLISECONDS);
Condition b = Condition.delayed(ctx -> true, 4000, TimeUnit.MILLISECONDS);
Condition condition = a.and(b);
ConditionContext ctx = ConditionContext.of();
condition.matches(ctx); // 👈 This will probably take about 7 seconds...
```

Below, let's take a look at making conditional expressions asynchronously.
```java
Condition a = Condition.delayed(ctx -> true, 3000, TimeUnit.MILLISECONDS).async(); // 👈
Condition b = Condition.delayed(ctx -> true, 4000, TimeUnit.MILLISECONDS).async(); // 👈
Condition condition = a.and(b);
ConditionContext ctx = ConditionContext.of();
condition.matches(ctx); // 👈 This will probably take about 4 seconds!
```

Alternatively, you can use `async` method from scratch like this:
```java
Condition.async(ctx -> true);
```

If you want to make all nested conditions asynchronous, you can also do like this:
```java
Condition a = Condition.delayed(ctx -> true, 3000, TimeUnit.MILLISECONDS);
Condition b = Condition.delayed(ctx -> true, 4000, TimeUnit.MILLISECONDS);
Condition condition = a.and(b).parallel(); // 👈
ConditionContext ctx = ConditionContext.of();
condition.matches(ctx); // 👈 This will probably take about 4 seconds!
```

Furthermore, even if the types of conditional expressions are different (synchronous or asynchronous), they can be composed.
```java
Condition sync = Condition.of(ctx -> true);
Condition async = Condition.async(ctx -> true);
sync.and(async); // 👈 You can combine synchronous and asynchronous conditions
async.and(sync); // 👈 You can combine asynchronous and synchronous conditions
```

## Easy to debug

`ConditionContext` contains useful information for debugging conditional expression.
Match logs of conditional expression can be seen in `ctx.logs()`.
Here, let's look at the match logs for asynchronous conditional expression.
```java
Condition a = Condition.async(ctx -> true).alias("a");
Condition b = Condition.async(ctx -> false).alias("b");
Condition condition = a.and(b);
ConditionContext ctx = ConditionContext.of();
condition.matches(ctx);

for (ConditionMatchResult log : ctx.logs()) { // 👈
    System.out.println(log);
}
// ConditionMatchResult{condition=a, state=COMPLETED, matches=true, async=true, thread=ForkJoinPool.commonPool-worker-1, delay=0ms, timeout=INF, startTime=2023-01-09T12:06:41.334+09:00, endTime=2023-01-09T12:06:41.334+09:00, duration=0ms}
// ConditionMatchResult{condition=b, state=COMPLETED, matches=false, async=true, thread=ForkJoinPool.commonPool-worker-2, delay=0ms, timeout=INF, startTime=2023-01-09T12:06:41.334+09:00, endTime=2023-01-09T12:06:41.334+09:00, duration=0ms}
// ConditionMatchResult{condition=(a and b), state=COMPLETED, matches=false, async=false, thread=Test worker, delay=0ms, timeout=INF, startTime=2023-01-09T12:06:41.332+09:00, endTime=2023-01-09T12:06:41.334+09:00, duration=2ms}
```

`ConditionMatchResult` provides detailed information about the matching process of each condition, such as the condition itself, its state, whether it matches, whether it's asynchronous, the thread it runs on, the delay, the timeout, the start time, the end time, and the duration.
You can see in which thread each conditional expression was matched, how long it took, and what the result was. Also, it is easy to know whether an exception was raised in the process of matching the conditional expression.

```java
Condition a = Condition.async(ctx -> true).alias("a");
Condition b = Condition.failed(ctx -> new IllegalStateException()).async().alias("b");
Condition condition = a.and(b);
ConditionContext ctx = ConditionContext.of();

try {
    condition.matches(ctx);
} catch (Exception e) {
    for (ConditionMatchResult log : ctx.logs()) { // 👈
        System.out.println(log);
    }
}
// ConditionMatchResult{condition=a, state=COMPLETED, matches=true, async=true, thread=ForkJoinPool.commonPool-worker-1, delay=0ms, timeout=INF, startTime=2023-01-09T12:07:00.489+09:00, endTime=2023-01-09T12:07:00.490+09:00, duration=1ms}
// ConditionMatchResult{condition=b, state=FAILED, cause=java.lang.IllegalStateException, async=true, thread=ForkJoinPool.commonPool-worker-2, delay=0ms, timeout=INF, startTime=2023-01-09T12:07:00.490+09:00, endTime=2023-01-09T12:07:00.490+09:00, duration=0ms}
// ConditionMatchResult{condition=(a and b), state=FAILED, cause=java.lang.IllegalStateException, async=false, thread=Test worker, delay=0ms, timeout=INF, startTime=2023-01-09T12:07:00.488+09:00, endTime=2023-01-09T12:07:00.490+09:00, duration=2ms}
```
In the above example, you can see that the condition `b` failed with an `IllegalStateException`, which is reflected in the `ConditionMatchResult`.

## Kotlin support
If you are using the [Kotlin programming language](https://kotlinlang.org), try to use _Conditional-Kotlin_.

To add a dependency on _Conditional-Kotlin_ using Gradle, use the following:
```groovy
dependencies {
    implementation("com.linecorp.conditional:conditional:1.1.3")
    implementation("com.linecorp.conditional:conditional-kotlin:1.1.3")
}
```

To add a dependency using Maven:
```xml
<dependencies>
    <dependency>
        <groupId>com.linecorp.conditional</groupId>
        <artifactId>conditional</artifactId>
        <version>1.1.3</version>
    </dependency>
    <dependency>
        <groupId>com.linecorp.conditional</groupId>
        <artifactId>conditional-kotlin</artifactId>
        <version>1.1.3</version>
    </dependency>  
</dependencies>
```

With Conditional-Kotlin, you can make conditional expressions like this:
```kotlin
val a: Condition = condition { ctx -> true }
val b: Condition = condition { true }
val condition: Condition = a and b
val ctx: ConditionContext = conditionContext()
assert(condition.matches(ctx) == true)
```

And it also supports [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html), so you can make more high-performance conditional expressions:
```kotlin
val a: CoroutineCondition = coroutineCondition { ctx -> true }
val b: CoroutineCondition = coroutineCondition { true }
val condition: CoroutineCondition = a and b
val ctx: CoroutineConditionContext = coroutineConditionContext()
assert(condition.matches(ctx) == true)
```
`CoroutineCondition` and `CoroutineConditionContext` are similar to `Condition` and `ConditionContext`, but they are designed to work with Kotlin's coroutines, which can provide more efficient asynchronous programming.

## Contribute
See [CONTRIBUTING](CONTRIBUTING.md).
If you believe you have discovered a vulnerability or have an issue related to security, please contact the maintainer directly or send us an [email](mailto:dl_oss_dev@linecorp.com) before sending a pull request.

## Contributors
See [the complete list of our contributors](https://github.com/line/conditional/contributors).

<a href="https://github.com/line/conditional/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=line/conditional" alt="Contributors" />
</a>

## License
```
   Copyright 2024 LINE Corporation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
See [LICENSE](LICENSE) for more details.
