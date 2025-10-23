package com.excel.calculator.impl.parser

internal sealed interface Token {
    data object Expression : Token

    data class DecimalNumber(
        val value: Double,
    ) : Token

    data class WholeNumber(
        val value: Long,
    ) : Token

    data class CellId(
        val value: String,
    ) : Token

    data class Function(
        val name: String,
    ) : Token

    data object Plus : Token

    data object BinaryMinus : Token

    data object UnaryMinus : Token

    data object Multiply : Token

    data object Divide : Token

    data object Comma : Token

    data object OpeningBracket : Token

    data object ClosingBracket : Token
}
