package com.excel.calculator.impl.parser

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

internal class TokenizerTest : DescribeSpec({

    val tokenizer = Tokenizer()

    describe("Simple Cases") {

        it("Empty") {
            tokenizer("") shouldBe TokenResult.Empty
        }

        it("Text") {
            tokenizer("some text") shouldBe
                TokenResult.Value(
                    "some text",
                )
        }
    }

    describe("Failures") {

        it("Empty Expression") {
            tokenizer("=") shouldBe
                TokenResult.Failure(
                    "Empty expression not allowed",
                )
        }

        it("Duplicit Expression") {
            tokenizer("=17=") shouldBe
                TokenResult.Failure(
                    "= is allowed only once at beginning",
                )
        }

        it("Invalid Cell Id") {
            tokenizer("=3A") shouldBe
                TokenResult.Failure(
                    "Wrong Cell Ids: 3A",
                )
        }
    }

    describe("Whole Number") {

        it("=17") {
            tokenizer("=17") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.WholeNumber(17),
                    ),
                )
        }

        it("=-17") {
            tokenizer("=-17") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.UnaryMinus,
                        Token.WholeNumber(17),
                    ),
                )
        }
    }

    describe("Decimal Number") {

        it("=5.5") {
            tokenizer("=5.5") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.DecimalNumber(5.5),
                    ),
                )
        }

        it("=-5.5") {
            tokenizer("=-5.5") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.UnaryMinus,
                        Token.DecimalNumber(5.5),
                    ),
                )
        }

        it("=.5") {
            tokenizer("=.5") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.DecimalNumber(0.5),
                    ),
                )
        }

        it("=-.5") {
            tokenizer("=-.5") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.UnaryMinus,
                        Token.DecimalNumber(0.5),
                    ),
                )
        }
    }

    describe("Cell Id") {

        it("=A3") {
            tokenizer("=A3") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.CellId("A3"),
                    ),
                )
        }

        it("=a3") {
            tokenizer("=a3") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.CellId("A3"),
                    ),
                )
        }

        it("=AB34") {
            tokenizer("=AB34") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.CellId("AB34"),
                    ),
                )
        }
    }

    describe("Minus Operator") {

        it("=-17") {
            tokenizer("=-17") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.UnaryMinus,
                        Token.WholeNumber(17),
                    ),
                )
        }

        it("=---17") {
            tokenizer("=---17") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.UnaryMinus,
                        Token.UnaryMinus,
                        Token.UnaryMinus,
                        Token.WholeNumber(17),
                    ),
                )
        }

        it("=17 - 5") {
            tokenizer("=17 - 5") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.WholeNumber(17),
                        Token.BinaryMinus,
                        Token.WholeNumber(5),
                    ),
                )
        }

        it("=-17 - ---5") {
            tokenizer("=-17 - ---5") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.UnaryMinus,
                        Token.WholeNumber(17),
                        Token.BinaryMinus,
                        Token.UnaryMinus,
                        Token.UnaryMinus,
                        Token.UnaryMinus,
                        Token.WholeNumber(5),
                    ),
                )
        }
    }

    describe("Other Operators") {

        it("=17 + 5") {
            tokenizer("=17 + 5") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.WholeNumber(17),
                        Token.Plus,
                        Token.WholeNumber(5),
                    ),
                )
        }

        it("=17 * 5") {
            tokenizer("=17 * 5") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.WholeNumber(17),
                        Token.Multiply,
                        Token.WholeNumber(5),
                    ),
                )
        }

        it("=17 / 5") {
            tokenizer("=17 / 5") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.WholeNumber(17),
                        Token.Divide,
                        Token.WholeNumber(5),
                    ),
                )
        }
    }

    describe("Named Function") {

        it("=pow(17, 5)") {
            tokenizer("=pow(17, 5)") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.Function("pow"),
                        Token.OpeningBracket,
                        Token.WholeNumber(17),
                        Token.Comma,
                        Token.WholeNumber(5),
                        Token.ClosingBracket,
                    ),
                )
        }

        it("=pow(1 + 7, A3 - 5)") {
            tokenizer("=pow(1 + 7, A3 - 5)") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.Function("pow"),
                        Token.OpeningBracket,
                        Token.WholeNumber(1),
                        Token.Plus,
                        Token.WholeNumber(7),
                        Token.Comma,
                        Token.CellId("A3"),
                        Token.BinaryMinus,
                        Token.WholeNumber(5),
                        Token.ClosingBracket,
                    ),
                )
        }
    }

    describe("Complex Formula") {

        it("=pow(-2, A1 - 3) * (42 + B2)") {
            tokenizer("=pow(-2, A1 - 3) * (42 + B2)") shouldBe
                TokenResult.Expression(
                    listOf(
                        Token.Function("pow"),
                        Token.OpeningBracket,
                        Token.UnaryMinus,
                        Token.WholeNumber(2),
                        Token.Comma,
                        Token.CellId("A1"),
                        Token.BinaryMinus,
                        Token.WholeNumber(3),
                        Token.ClosingBracket,
                        Token.Multiply,
                        Token.OpeningBracket,
                        Token.WholeNumber(42),
                        Token.Plus,
                        Token.CellId("B2"),
                        Token.ClosingBracket,
                    ),
                )
        }
    }
})
