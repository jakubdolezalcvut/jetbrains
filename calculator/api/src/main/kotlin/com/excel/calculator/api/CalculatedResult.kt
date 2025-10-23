package com.excel.calculator.api

import java.math.BigDecimal

@JvmInline
value class CalculatedResult(
    val value: BigDecimal,
) {
    companion object {
        val ZERO = CalculatedResult(value = BigDecimal.ZERO)

        fun Double.toCalculatedResult() =
            CalculatedResult(BigDecimal.valueOf(this))

        fun Int.toCalculatedResult() =
            toLong().toCalculatedResult()

        fun Long.toCalculatedResult() =
            CalculatedResult(BigDecimal.valueOf(this))
    }

    operator fun plus(other: CalculatedResult) =
        CalculatedResult(value = value + other.value)

    operator fun minus(other: CalculatedResult) =
        CalculatedResult(value = value - other.value)

    operator fun times(other: CalculatedResult) =
        CalculatedResult(value = value * other.value)

    operator fun div(other: CalculatedResult) =
        CalculatedResult(value = value / other.value)

    operator fun unaryMinus() =
        CalculatedResult(value = -value)

    fun pow(exponent: CalculatedResult) =
        CalculatedResult(value = value.pow(exponent.value.intValueExact()))
}
