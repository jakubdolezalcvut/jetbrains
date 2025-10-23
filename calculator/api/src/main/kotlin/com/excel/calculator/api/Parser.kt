package com.excel.calculator.api

interface Parser {

    operator fun invoke(expression: String): ParseResult
}
