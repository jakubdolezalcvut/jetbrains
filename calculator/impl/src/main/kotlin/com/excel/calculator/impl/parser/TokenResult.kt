package com.excel.calculator.impl.parser

internal sealed interface TokenResult {

    data object Empty : TokenResult

    data class Expression(
        val tokens: List<Token>,
    ) : TokenResult

    data class Value(
        val text: String,
    ) : TokenResult

    data class Failure(
        val message: String,
    ) : TokenResult
}
