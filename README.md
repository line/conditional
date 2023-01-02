# Conditional

> Make your own conditional expressions more elegant.

[![build](https://github.com/line/conditional/actions/workflows/gradle.yml/badge.svg?branch=main)](https://github.com/line/conditional/actions/workflows/gradle.yml)
<a href="https://github.com/line/conditional/contributors"><img src="https://img.shields.io/github/contributors/line/conditional.svg"></a>
<a href="https://search.maven.org/search?q=g:com.linecorp.conditional%20AND%20a:conditional"><img src="https://img.shields.io/maven-central/v/com.linecorp.conditional/conditional.svg?label=version"></a>
<a href="https://github.com/line/conditional/commits"><img src="https://img.shields.io/github/release-date/line/conditional.svg?label=release"></a>
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

_Conditional_ is a super lightweight library that helps you make conditional expressions. You can compose multiple conditional expressions and make them asynchronous easily.

## Why do we need Conditional?

Let's make a simple conditional expression to see how _Conditional_ is useful:
```
a and (b or c)
```

An asynchronous implementation of this by [CompletableFuture](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/CompletableFuture.html) would be:
```java
var a = CompletableFuture.supplyAsync(() -> true);
var b = CompletableFuture.supplyAsync(() -> true);
var c = CompletableFuture.supplyAsync(() -> true);
var future = a.thenCombine(b.thenCombine(c, (rb, rc) -> rb || rc), (ra, rbc) -> ra && rbc);
assert future.join() == true;
```

It's a simple conditional expression, but using [CompletableFuture.thenCombine](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/CompletableFuture.html#thenCombine(java.util.concurrent.CompletionStage,java.util.function.BiFunction)) well is quite complex.
Let's try to use _Conditional_ to simplify this asynchronous implementation:
```java
var a = Condition.of(ctx -> true);
var b = Condition.of(ctx -> true);
var c = Condition.of(ctx -> true);
var condition = a.and(b.or(c));
var ctx = ConditionContext.of();
assert condition.parallel().matches(ctx) == true;
```

Much more readable than using [CompletableFuture.thenCombine](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/concurrent/CompletableFuture.html#thenCombine(java.util.concurrent.CompletionStage,java.util.function.BiFunction)).
Also, if we are using the [Kotlin programming language](https://kotlinlang.org), we can make it even simpler with Kotlin DSL support of _Conditional_:
```kotlin
val a = condition { true }
val b = condition { true }
val c = condition { true }
val condition = a and (b or c)
val ctx = conditionContext()
assert(condition.parallel().matches(ctx) == true)
```

As above, we can make conditional expressions more elegant by using _Conditional_.
Let's dive into the _Conditional_.

## Getting started

To add a dependency on _Conditional_ using Gradle, use the following:
```groovy
dependencies {
    implementation("com.linecorp.conditional:conditional:1.0.3")
}
```

To add a dependency using Maven:
```xml
<dependency>
    <groupId>com.linecorp.conditional</groupId>
    <artifactId>conditional</artifactId>
    <version>1.0.3</version>
</dependency>
```

It takes only 3 steps to make and match a conditional expression.
```java
// Step 1: Make a conditional expression.
var a = Condition.of(ctx -> ctx.var("a", Boolean.class));
var b = Condition.of(ctx -> ctx.var("b", Boolean.class));
var condition = a.and(b);

// Step 2: Make a context for matching conditional expression.
var ctx = ConditionContext.of("a", true, "b", true);

// Step 3: Match a conditional expression.
assert condition.matches(ctx) == true;
```

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

If timeout is exceeded, a `TimeoutException` is raised.
```java
var condition = Condition.of(ctx -> {
    try {
        TimeUnit.MILLISECONDS.sleep(5000);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    return true;
}).timeout(3000, TimeUnit.MILLISECONDS);
var ctx = ConditionContext.of();
condition.matches(ctx); // 👈 TimeoutException will be raised after 3 seconds.
```

You can also set timeout for more complex conditional expression.
```java
var a = Condition.of(ctx -> {
    try {
        TimeUnit.MILLISECONDS.sleep(3000);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    return true;
}).timeout(3500, TimeUnit.MILLISECONDS);
var b = Condition.of(ctx -> {
    try {
        TimeUnit.MILLISECONDS.sleep(4500);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
    return true;
}).timeout(4000, TimeUnit.MILLISECONDS);
var condition = a.and(b).timeout(8000, TimeUnit.MILLISECONDS);
var ctx = ConditionContext.of();
condition.matches(ctx); // 👈 TimeoutException will be raised due to 'b' after 4 seconds.
```

## Asynchronous support

_Conditional_ supports asynchronous for higher performance in I/O intensive operations. First, let's look at the simple synchronous conditional expressions.
```java
var a = Condition.delayed(ctx -> true, 3000, TimeUnit.MILLISECONDS);
var b = Condition.delayed(ctx -> true, 4000, TimeUnit.MILLISECONDS);
var condition = a.and(b);
var ctx = ConditionContext.of();
condition.matches(ctx); // 👈 This will probably take about 7 seconds...
```

Below, let's take a look at making conditional expressions asynchronously.
```java
var a = Condition.delayed(ctx -> true, 3000, TimeUnit.MILLISECONDS).async(); // 👈
var b = Condition.delayed(ctx -> true, 4000, TimeUnit.MILLISECONDS).async(); // 👈
var condition = a.and(b);
var ctx = ConditionContext.of();
condition.matches(ctx); // 👈 This will probably take about 4 seconds!
```

Alternatively, you can use `async` method from scratch like this:
```java
Condition.async(ctx -> true);
```

If you want to make all nested conditions asynchronous, you can also do like this:
```java
var a = Condition.delayed(ctx -> true, 3000, TimeUnit.MILLISECONDS);
var b = Condition.delayed(ctx -> true, 4000, TimeUnit.MILLISECONDS);
var condition = a.and(b).parallel(); // 👈
var ctx = ConditionContext.of();
condition.matches(ctx); // 👈 This will probably take about 4 seconds!
```

Furthermore, even if the colors of conditional expressions are different, they can be composed.
```java
var sync = Condition.of(ctx -> true);
var async = Condition.async(ctx -> true);
sync.and(async); // 👈
async.and(sync); // 👈
```

## Easy to debug

`ConditionContext` contains useful information for debugging conditional expression.
Match logs of conditional expression can be seen in `ctx.logs()`. Here, let's look at the match logs for asynchronous conditional expression.
```java
var a = Condition.async(ctx -> true).alias("a");
var b = Condition.async(ctx -> false).alias("b");
var condition = a.and(b);
var ctx = ConditionContext.of();
condition.matches(ctx);

for (var log : ctx.logs()) { // 👈
    System.out.println(log);
}
// ConditionMatchCompletion{condition=a, matches=true, async=true, thread=ForkJoinPool.commonPool-worker-1, delay=0ms, timeout=INF, startTime=1672051484770ms, endTime=1672051484770ms, duration=0ms}
// ConditionMatchCompletion{condition=b, matches=false, async=true, thread=ForkJoinPool.commonPool-worker-2, delay=0ms, timeout=INF, startTime=1672051484770ms, endTime=1672051484770ms, duration=0ms}
// ConditionMatchCompletion{condition=(a and b), matches=false, async=false, thread=Test worker, delay=0ms, timeout=INF, startTime=1672051484768ms, endTime=1672051484770ms, duration=2ms}
```

You can see in which thread each conditional expression was matched, how long it took, and what the result was.
Also, it is easy to know whether an exception was raised in the process of matching the conditional expression.

```java
var a = Condition.async(ctx -> true).alias("a");
var b = Condition.failed(ctx -> new RuntimeException()).async().alias("b");
var condition = a.and(b);
var ctx = ConditionContext.of();

try {
    condition.matches(ctx);
} catch (Exception e) {
    for (var log : ctx.logs()) { // 👈
        System.out.println(log);
    }
}
// ConditionMatchCompletion{condition=a, matches=true, async=true, thread=ForkJoinPool.commonPool-worker-1, delay=0ms, timeout=INF, startTime=1672051528775ms, endTime=1672051528775ms, duration=0ms}
// ConditionMatchFailure{condition=b, cause=java.lang.IllegalStateException, async=true, thread=ForkJoinPool.commonPool-worker-2, delay=0ms, timeout=INF, startTime=1672051528776ms, endTime=1672051528776ms, duration=0ms}
// ConditionMatchFailure{condition=(a and b), cause=java.util.concurrent.CompletionException: java.lang.IllegalStateException, async=false, thread=Test worker, delay=0ms, timeout=INF, startTime=1672051528774ms, endTime=1672051528776ms, duration=2ms}
```

## Kotlin DSL support
If you are using the [Kotlin programming language](https://kotlinlang.org), try to use _Conditional-Kotlin_. It makes it easier for you to make conditional expressions using the Kotlin DSL.

To add a dependency on _Conditional-Kotlin_ using Gradle, use the following:
```groovy
dependencies {
    implementation("com.linecorp.conditional:conditional:1.0.3")
    implementation("com.linecorp.conditional:conditional-kotlin:1.0.3")
}
```

To add a dependency using Maven:
```xml
<dependencies>
    <dependency>
        <groupId>com.linecorp.conditional</groupId>
        <artifactId>conditional</artifactId>
        <version>1.0.3</version>
    </dependency>
    <dependency>
        <groupId>com.linecorp.conditional</groupId>
        <artifactId>conditional-kotlin</artifactId>
        <version>1.0.3</version>
    </dependency>  
</dependencies>
```

With _Conditional-Kotlin_, you can make conditional expressions like this:
```kotlin
val a = condition { ctx -> true }
val b = condition { true }
val condition = a and b
val ctx = conditionContext()
condition.matches(ctx)
```

If you want to know more about other DSLs provided by _Conditional-Kotlin_, please refer to [this](https://github.com/line/conditional/blob/main/conditional-kotlin/src/main/kotlin/com/linecorp/conditional/kotlin/ConditionalExtension.kt).

## How to contribute
See [CONTRIBUTING](CONTRIBUTING.md).
If you believe you have discovered a vulnerability or have an issue related to security, please contact the maintainer directly or send us an [email](mailto:dl_oss_dev@linecorp.com) before sending a pull request.

## License
```
   Copyright 2022 LINE Corporation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
See [LICENSE](LICENSE) for more details.

## Contributors
See [the complete list of our contributors](https://github.com/line/conditional/contributors).

<a href="https://github.com/line/conditional/graphs/contributors">
    <img src="https://contrib.rocks/image?repo=line/conditional" alt="Contributors" />
</a>