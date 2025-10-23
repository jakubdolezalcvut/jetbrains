package com.excel.calculator.impl.parser

import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

internal class Tokenizer {
    private object Pattern {
        const val WhiteSpace = """\s+"""
        const val WrongCellId = """\d+[a-zA-Z]+"""

        const val Expression = "="
        const val DecimalNumber = """\d*\.\d+"""
        const val WholeNumber = """\d+"""
        const val CellId = """[a-zA-Z]+\d+"""
        const val Function = """[a-zA-Z]+"""
        const val Plus = "+"
        const val Minus = "-"
        const val Multiply = "*"
        const val Divide = "/"
        const val Comma = ","
        const val OpeningBracket = "("
        const val ClosingBracket = ")"
    }

    private object Sanitation {
        val WhiteSpace = Pattern.WhiteSpace.toRegex()
        val WrongCellId = Pattern.WrongCellId.toRegex()
    }

    private object Expression {
        val DecimalNumber = Pattern.DecimalNumber.toRegex()
        val WholeNumber = Pattern.WholeNumber.toRegex()
        val CellId = Pattern.CellId.toRegex()
        val Function = Pattern.Function.toRegex()

        val Tokens = listOf(
            Pattern.Expression,
            DecimalNumber,
            WholeNumber,
            Pattern.CellId,
            Pattern.Function,
            """\${Pattern.Plus}""",
            """\${Pattern.Minus}""",
            """\${Pattern.Multiply}""",
            """\${Pattern.Divide}""",
            """\${Pattern.Comma}""",
            """\${Pattern.OpeningBracket}""",
            """\${Pattern.ClosingBracket}""",
        ).joinToString(separator = "|").toRegex()
    }

    operator fun invoke(expression: String): TokenResult {
        val withoutSpaces = expression.replace(Sanitation.WhiteSpace, "")
        val invalidCellIds = findInvalidCellId(withoutSpaces)

        if (invalidCellIds.isNotEmpty()) {
            logger.error { "Wrong Cell Ids: $invalidCellIds" }
            return TokenResult.Failure("Wrong Cell Ids: ${invalidCellIds.joinToString()}")
        }
        val values = split(withoutSpaces)

        val tokens = try {
            tokenize(values)
        } catch (exception: UnknownTokenException) {
            logger.error(exception) { "Unknown token: ${exception.token}" }
            return TokenResult.Failure("Unknown token: ${exception.token}")
        }
        return analyzeTokens(tokens, expression)
    }

    private fun analyzeTokens(
        tokens: List<Token>,
        expression: String,
    ): TokenResult = when {
        tokens.isEmpty() -> {
            TokenResult.Empty
        }
        (tokens.size == 1) && (tokens[0] == Token.Expression) -> {
            TokenResult.Failure("Empty expression not allowed")
        }
        tokens.count { token -> token == Token.Expression } > 1 -> {
            TokenResult.Failure("= is allowed only once at beginning")
        }
        (tokens[0] == Token.Expression) -> {
            TokenResult.Expression(tokens.subList(1, tokens.size))
        }
        else -> {
            TokenResult.Value(expression)
        }
    }

    private fun findInvalidCellId(withoutSpaces: String): List<String> =
        Sanitation.WrongCellId
            .findAll(withoutSpaces)
            .map { matchResult -> matchResult.value }
            .filter { value -> value.matches(Sanitation.WrongCellId) }
            .toList()

    private fun split(withoutSpaces: String): List<String> =
        Expression.Tokens
            .findAll(withoutSpaces)
            .map { matchResult -> matchResult.value }
            .toList()

    private fun tokenize(values: List<String>): List<Token> {
        var unaryMinusEnabled = true

        return values.map { value ->
            when {
                value == Pattern.Expression -> {
                    unaryMinusEnabled = true
                    Token.Expression
                }
                value.matches(Expression.DecimalNumber) -> {
                    unaryMinusEnabled = false
                    Token.DecimalNumber(value.toDouble())
                }
                value.matches(Expression.WholeNumber) -> {
                    unaryMinusEnabled = false
                    Token.WholeNumber(value.toLong())
                }
                value.matches(Expression.CellId) -> {
                    unaryMinusEnabled = false
                    Token.CellId(value.uppercase())
                }
                value.matches(Expression.Function) -> {
                    // unaryMinusEnabled not applicable
                    Token.Function(value)
                }
                value == Pattern.Plus -> {
                    unaryMinusEnabled = true
                    Token.Plus
                }
                value == Pattern.Minus -> {
                    if (unaryMinusEnabled) {
                        // unaryMinusEnabled not applicable
                        Token.UnaryMinus
                    } else {
                        unaryMinusEnabled = true
                        Token.BinaryMinus
                    }
                }
                value == Pattern.Multiply -> {
                    unaryMinusEnabled = true
                    Token.Multiply
                }
                value == Pattern.Divide -> {
                    unaryMinusEnabled = true
                    Token.Divide
                }
                value == Pattern.Comma -> {
                    unaryMinusEnabled = true
                    Token.Comma
                }
                value == Pattern.OpeningBracket -> {
                    unaryMinusEnabled = true
                    Token.OpeningBracket
                }
                value == Pattern.ClosingBracket -> {
                    unaryMinusEnabled = false
                    Token.ClosingBracket
                }
                else -> {
                    throw UnknownTokenException(value)
                }
            }
        }
    }
}
