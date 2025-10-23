package com.excel.calculator.api

sealed interface ParseResult {

    data object Empty : ParseResult

    data class Success(
        val node: Node,
    ) : ParseResult

    data class Failure(
        val message: String,
    ) : ParseResult
}
