package com.excel.calculator.impl.parser

import com.excel.calculator.api.BinaryFunction
import com.excel.calculator.api.CellId
import com.excel.calculator.api.CellId.Companion.toCellId
import com.excel.calculator.api.NamedFunction
import com.excel.calculator.api.Node
import com.excel.calculator.api.ParseResult
import com.excel.calculator.api.Parser
import com.excel.calculator.api.TextNode
import com.excel.calculator.api.UnaryFunction
import com.excel.calculator.api.ValueHolder
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Deque
import java.util.LinkedList
import java.util.Queue

private val logger = KotlinLogging.logger {}

/**
 * The following syntactic constructs should be supported:
 * - parentheses,
 * - binary operators,
 * - unary operators,
 * - named functions,
 * - references to table cells.
 * You may choose the set of supported operators and named functions yourself.
 * Example formula:
 * =pow(-2, A1 - 3) * (42 + B2)
 */
internal class ParserImpl(
    private val tokenizer: Tokenizer,
) : Parser {

    private object Pattern {
        val WholeNumber = """-?\d+""".toRegex()
        val DecimalNumber = """-?\d*\.\d+""".toRegex()
    }

    private object Function {
        const val Power = "pow"
    }

    override operator fun invoke(expression: String): ParseResult =
        when (val tokenResult = tokenizer(expression)) {
            is TokenResult.Empty -> {
                ParseResult.Empty
            }
            is TokenResult.Expression -> {
                parseExpression(tokenResult.tokens)
            }
            is TokenResult.Value -> {
                parseLiteral(tokenResult.text)
            }
            is TokenResult.Failure -> {
                ParseResult.Failure(tokenResult.message)
            }
        }

    private fun parseLiteral(text: String): ParseResult =
        try {
            val node = when {
                text.matches(Pattern.WholeNumber) -> {
                    ValueHolder.WholeNumber(text.toLong())
                }

                text.matches(Pattern.DecimalNumber) -> {
                    ValueHolder.DecimalNumber(text.toDouble())
                }

                else -> {
                    TextNode(text)
                }
            }
            ParseResult.Success(node)
        } catch (exception: Exception) {
            logger.error(exception) { "Can't parse: $text" }
            ParseResult.Failure("Can't parse '$text' because ${exception.message}")
        }

    private fun parseExpression(tokens: List<Token>): ParseResult {
        val linearTokens: Queue<Token> = LinkedList(tokens)

        return try {
            val infixTokens = handleInfixOperations(linearTokens)
            val node = buildNodes(infixTokens)
            ParseResult.Success(node)
        } catch (exception: UnknownTextException) {
            logger.error(exception) { "Unknown text: ${exception.text}" }
            ParseResult.Failure("Unknown text: ${exception.text}")
        } catch (exception: Exception) {
            logger.error(exception) { "Can't parse: $tokens" }
            ParseResult.Failure("Can't parse because ${exception.message}")
        }
    }

    private fun handleInfixOperations(tokens: Queue<Token>): Deque<Token> {
        val stack = LinkedList<Token>()
        var tokensMoveCount = 0
        var namedFunction: Token? = null

        while (tokens.isNotEmpty()) {
            when (val token = tokens.remove()) {
                is Token.WholeNumber,
                is Token.DecimalNumber,
                is Token.CellId,
                -> {
                    val movedTokens = mutableListOf<Token>()
                    repeat(tokensMoveCount) {
                        movedTokens += stack.pop()
                    }
                    stack.push(token)
                    movedTokens.forEach { movedToken ->
                        stack.push(movedToken)
                    }
                    tokensMoveCount = 0
                }
                is Token.Plus,
                is Token.BinaryMinus,
                is Token.UnaryMinus,
                is Token.Multiply,
                is Token.Divide,
                -> {
                    stack.push(token)
                    tokensMoveCount++
                }
                is Token.Function -> {
                    stack.push(token)
                    namedFunction = token
                }
                is Token.OpeningBracket -> {
                    // Nothing
                }
                is Token.ClosingBracket -> {
                    if (namedFunction != null) {
                        stack.remove(namedFunction)
                        stack.push(namedFunction)
                        namedFunction = null
                    }
                }
                is Token.Comma -> {
                    // Nothing
                }
                is Token.Expression -> {
                    // Nothing
                }
            }
        }
        return stack
    }

    private fun buildNodes(tokens: Deque<Token>): Node =
        when (val token = tokens.pop()) {
            is Token.WholeNumber -> {
                ValueHolder.WholeNumber(token.value)
            }
            is Token.DecimalNumber -> {
                ValueHolder.DecimalNumber(token.value)
            }
            is Token.CellId -> {
                ValueHolder.Reference(token.value.toCellId())
            }
            is Token.Plus -> {
                BinaryFunction.Plus(
                    right = buildNodes(tokens),
                    left = buildNodes(tokens),
                )
            }
            is Token.BinaryMinus -> {
                BinaryFunction.Minus(
                    right = buildNodes(tokens),
                    left = buildNodes(tokens),
                )
            }
            is Token.UnaryMinus -> {
                UnaryFunction.Minus(
                    value = buildNodes(tokens),
                )
            }
            is Token.Multiply -> {
                BinaryFunction.Multiply(
                    right = buildNodes(tokens),
                    left = buildNodes(tokens),
                )
            }
            is Token.Divide -> {
                BinaryFunction.Divide(
                    right = buildNodes(tokens),
                    left = buildNodes(tokens),
                )
            }
            is Token.Function -> {
                when (token.name) {
                    Function.Power ->
                        NamedFunction.Power(
                            exponent = buildNodes(tokens),
                            base = buildNodes(tokens),
                        )
                    else -> throw UnknownTextException(token.name)
                }
            }
            is Token.OpeningBracket -> {
                error("OpeningBracket not allowed")
            }
            is Token.ClosingBracket -> {
                error("ClosingBracket not allowed")
            }
            is Token.Comma -> {
                error("Comma not allowed")
            }
            is Token.Expression -> {
                error("Expression not allowed")
            }
        }
}
