package com.linecorp.conditional.kotlin

import com.linecorp.conditional.ComposedCondition
import com.linecorp.conditional.Condition

infix fun Condition.and(condition: Condition): ComposedCondition = this.and(condition)

infix fun Condition.or(condition: Condition): ComposedCondition = this.or(condition)

operator fun Condition.not(): Condition = Condition.not(this)
