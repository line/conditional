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
(a and b) or (c and d)
```

An asynchronous implementation of this would be:
```java
CompletableFuture<Boolean> a = CompletableFuture.supplyAsync(() -> true);
CompletableFuture<Boolean> b = CompletableFuture.supplyAsync(() -> true);
CompletableFuture<Boolean> c = CompletableFuture.supplyAsync(() -> true);
CompletableFuture<Boolean> d = CompletableFuture.supplyAsync(() -> true);
CompletableFuture<Boolean> future =
        a.thenCombine(b, (ra, rb) -> ra && rb)
         .thenCombine(c.thenCombine(d, (rc, rd) -> rc && rd), (rab, rcd) -> rab || rcd); // 👈
assert future.join() == true;
```

It's a simple conditional expression, but not trivial to implement asynchronously.
Let's try to use _Conditional_ to simplify this asynchronous implementation:
```java
Condition a = Condition.of(ctx -> true);
Condition b = Condition.of(ctx -> true);
Condition c = Condition.of(ctx -> true);
Condition d = Condition.of(ctx -> true);
Condition condition = (a.and(b)).or(c.and(d)); // 👈
ConditionContext ctx = ConditionContext.of();
assert condition.parallel().matches(ctx) == true;
```

It's much more readable than before. And if we are using the [Kotlin programming language](https://kotlinlang.org), we can make it even simpler with Kotlin DSL support of _Conditional_:
```kotlin
val a: Condition = condition { true }
val b: Condition = condition { true }
val c: Condition = condition { true }
val d: Condition = condition { true }
val condition: Condition = (a and b) or (c and d) // 👈
val ctx: ConditionContext = conditionContext()
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
Condition a = Condition.of(ctx -> ctx.var("a", Boolean.class));
Condition b = Condition.of(ctx -> ctx.var("b", Boolean.class));
Condition condition = a.and(b);

// Step 2: Make a context for matching conditional expression.
ConditionContext ctx = ConditionContext.of("a", true, "b", true);

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

## Asynchronous support

_Conditional_ supports asynchronous for higher performance in I/O intensive operations. First, let's look at the simple synchronous conditional expressions.
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

Furthermore, even if the colors of conditional expressions are different, they can be composed.
```java
Condition sync = Condition.of(ctx -> true);
Condition async = Condition.async(ctx -> true);
sync.and(async); // 👈
async.and(sync); // 👈
```

## Less computation
_Conditional_ is optimized to do less computation. For example:
```java
// (a and b) or (c and d)
CompletableFuture<Boolean> a = CompletableFuture.supplyAsync(() -> { sleep(3000); return true; });
CompletableFuture<Boolean> b = CompletableFuture.supplyAsync(() -> { sleep(1000); return false; });
CompletableFuture<Boolean> c = CompletableFuture.supplyAsync(() -> { sleep(1500); return false; });
CompletableFuture<Boolean> d = CompletableFuture.supplyAsync(() -> { sleep(2500); return true; });
CompletableFuture<Boolean> future =
        a.thenCombine(b, (ra, rb) -> ra && rb)
         .thenCombine(c.thenCombine(d, (rc, rd) -> rc && rd), (rab, rcd) -> rab || rcd);
future.join(); // 👈 It takes about 3000 milliseconds.
```

Let's implement the above conditional expression as _Conditional_.
```java
// (a and b) or (c and d)
final var a = Condition.of(ctx -> { sleep(3000); return true; });
final var b = Condition.of(ctx -> { sleep(1000); return false; });
final var c = Condition.of(ctx -> { sleep(1500); return false; });
final var d = Condition.of(ctx -> { sleep(2500); return true; });
final var condition = (a.and(b)).or(c.and(d));
final var ctx = ConditionContext.of();
condition.parallel().matches(ctx); // 👈 It takes about 1500 milliseconds.
```

Why is there such a difference in execution time?
Looking at the conditional expression above, `b` and `c` are false. So, regardless of the results of `a` and `d`, the result of the entire conditional expression is false.
In this case, _Conditional_ does performance optimization internally for less computation, so the match result can be returned faster.

## Easy to debug

`ConditionContext` contains useful information for debugging conditional expression.
Match logs of conditional expression can be seen in `ctx.logs()`. Here, let's look at the match logs for asynchronous conditional expression.
```java
Condition a = Condition.async(ctx -> true).alias("a");
Condition b = Condition.async(ctx -> false).alias("b");
Condition condition = a.and(b);
ConditionContext ctx = ConditionContext.of();
condition.matches(ctx);

for (ConditionMatchResult log : ctx.logs()) { // 👈
    System.out.println(log);
}
// ConditionMatchCompletion{condition=a, matches=true, async=true, thread=ForkJoinPool.commonPool-worker-1, delay=0ms, timeout=INF, startTime=1672051484770ms, endTime=1672051484770ms, duration=0ms}
// ConditionMatchCompletion{condition=b, matches=false, async=true, thread=ForkJoinPool.commonPool-worker-2, delay=0ms, timeout=INF, startTime=1672051484770ms, endTime=1672051484770ms, duration=0ms}
// ConditionMatchCompletion{condition=(a and b), matches=false, async=false, thread=Test worker, delay=0ms, timeout=INF, startTime=1672051484768ms, endTime=1672051484770ms, duration=2ms}
```

You can see in which thread each conditional expression was matched, how long it took, and what the result was.
Also, it is easy to know whether an exception was raised in the process of matching the conditional expression.

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

       https://www.apache.org/licenses/LICENSE-2.0

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