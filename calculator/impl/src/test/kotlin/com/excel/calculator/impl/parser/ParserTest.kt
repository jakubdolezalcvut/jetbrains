package com.excel.calculator.impl.parser

import com.excel.calculator.api.BinaryFunction
import com.excel.calculator.api.CellId.Companion.toCellId
import com.excel.calculator.api.NamedFunction
import com.excel.calculator.api.ParseResult
import com.excel.calculator.api.TextNode
import com.excel.calculator.api.UnaryFunction
import com.excel.calculator.api.ValueHolder
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

internal class ParserTest : DescribeSpec({

    val parser = ParserImpl(
        tokenizer = Tokenizer(),
    )

    describe("Simple Cases") {

        it("Empty") {
            parser("") shouldBe ParseResult.Empty
        }
    }

    describe("Recognized as Text") {

        it("--17") {
            parser("--17") shouldBe
                ParseResult.Success(
                    TextNode("--17"),
                )
        }

        it("--5.5") {
            parser("--5.5") shouldBe
                ParseResult.Success(
                    TextNode("--5.5"),
                )
        }

        it("--.5") {
            parser("--.5") shouldBe
                ParseResult.Success(
                    TextNode("--.5"),
                )
        }

        it("A3") {
            parser("A3") shouldBe
                ParseResult.Success(
                    TextNode("A3"),
                )
        }

        it("some text") {
            parser("some text") shouldBe
                ParseResult.Success(
                    TextNode("some text"),
                )
        }
    }

    describe("Failures") {

        it("Empty Expression") {
            parser("=") shouldBe
                ParseResult.Failure(
                    "Empty expression not allowed",
                )
        }

        it("Text in Expression") {
            parser("=some text") shouldBe
                ParseResult.Failure(
                    "Unknown text: sometext",
                )
        }

        it("Invalid Cell Id") {
            parser("=3A") shouldBe
                ParseResult.Failure(
                    "Wrong Cell Ids: 3A",
                )
        }
    }

    describe("Literal") {

        describe("Whole Number") {

            it("17") {
                parser("17") shouldBe
                    ParseResult.Success(
                        ValueHolder.WholeNumber(17),
                    )
            }

            it("-17") {
                parser("-17") shouldBe
                    ParseResult.Success(
                        ValueHolder.WholeNumber(-17),
                    )
            }
        }

        describe("Decimal Number") {

            it("5.5") {
                parser("5.5") shouldBe
                    ParseResult.Success(
                        ValueHolder.DecimalNumber(5.5),
                    )
            }

            it("-5.5") {
                parser("-5.5") shouldBe
                    ParseResult.Success(
                        ValueHolder.DecimalNumber(-5.5),
                    )
            }

            it(".5") {
                parser(".5") shouldBe
                    ParseResult.Success(
                        ValueHolder.DecimalNumber(0.5),
                    )
            }

            it("-.5") {
                parser("-.5") shouldBe
                    ParseResult.Success(
                        ValueHolder.DecimalNumber(-0.5),
                    )
            }
        }
    }

    describe("Expression") {

        describe("Whole Number") {

            it("=17") {
                parser("=17") shouldBe
                    ParseResult.Success(
                        ValueHolder.WholeNumber(17),
                    )
            }
        }

        describe("Decimal Number") {

            it("=5.5") {
                parser("=5.5") shouldBe
                    ParseResult.Success(
                        ValueHolder.DecimalNumber(5.5),
                    )
            }

            it("=.5") {
                parser(".5") shouldBe
                    ParseResult.Success(
                        ValueHolder.DecimalNumber(0.5),
                    )
            }
        }

        describe("Reference") {

            it("=A3") {
                parser("=A3") shouldBe
                    ParseResult.Success(
                        ValueHolder.Reference("A3".toCellId()),
                    )
            }

            it("=AA33") {
                parser("=AA33") shouldBe
                    ParseResult.Success(
                        ValueHolder.Reference("AA33".toCellId()),
                    )
            }
        }
    }

    describe("Operators") {

        describe("Minus") {

            it("=-17") {
                parser("=-17") shouldBe
                    ParseResult.Success(
                        UnaryFunction.Minus(
                            value = ValueHolder.WholeNumber(17),
                        ),
                    )
            }

            it("=---17") {
                parser("=---17") shouldBe
                    ParseResult.Success(
                        UnaryFunction.Minus(
                            value =
                                UnaryFunction.Minus(
                                    value =
                                        UnaryFunction.Minus(
                                            value = ValueHolder.WholeNumber(17),
                                        ),
                                ),
                        ),
                    )
            }

            it("=-5.5") {
                parser("=-5.5") shouldBe
                    ParseResult.Success(
                        UnaryFunction.Minus(
                            value = ValueHolder.DecimalNumber(5.5),
                        ),
                    )
            }

            it("=---5.5") {
                parser("=---5.5") shouldBe
                    ParseResult.Success(
                        UnaryFunction.Minus(
                            value =
                                UnaryFunction.Minus(
                                    value =
                                        UnaryFunction.Minus(
                                            value = ValueHolder.DecimalNumber(5.5),
                                        ),
                                ),
                        ),
                    )
            }

            it("=17 - 5") {
                parser("=17 - 5") shouldBe
                    ParseResult.Success(
                        BinaryFunction.Minus(
                            left = ValueHolder.WholeNumber(17),
                            right = ValueHolder.WholeNumber(5),
                        ),
                    )
            }

            it("=-17 - ---5") {
                parser("=-17 - ---5") shouldBe
                    ParseResult.Success(
                        BinaryFunction.Minus(
                            left =
                                UnaryFunction.Minus(
                                    value = ValueHolder.WholeNumber(17),
                                ),
                            right =
                                UnaryFunction.Minus(
                                    value =
                                        UnaryFunction.Minus(
                                            value =
                                                UnaryFunction.Minus(
                                                    value = ValueHolder.WholeNumber(5),
                                                ),
                                        ),
                                ),
                        ),
                    )
            }
        }

        describe("Plus between various Value Holders") {

            it("=17 + 5") {
                parser("=17 + 5") shouldBe
                    ParseResult.Success(
                        BinaryFunction.Plus(
                            left = ValueHolder.WholeNumber(17),
                            right = ValueHolder.WholeNumber(5),
                        ),
                    )
            }

            it("=17 + A3") {
                parser("=17 + A3") shouldBe
                    ParseResult.Success(
                        BinaryFunction.Plus(
                            left = ValueHolder.WholeNumber(17),
                            right = ValueHolder.Reference("A3".toCellId()),
                        ),
                    )
            }

            it("=A3 + 17") {
                parser("=A3 + 17") shouldBe
                    ParseResult.Success(
                        BinaryFunction.Plus(
                            left = ValueHolder.Reference("A3".toCellId()),
                            right = ValueHolder.WholeNumber(17),
                        ),
                    )
            }

            it("=A3 + F7") {
                parser("=A3 + F7") shouldBe
                    ParseResult.Success(
                        BinaryFunction.Plus(
                            left = ValueHolder.Reference("A3".toCellId()),
                            right = ValueHolder.Reference("F7".toCellId()),
                        ),
                    )
            }
        }

        describe("Multiply") {

            it("=17 * 5") {
                parser("=17 * 5") shouldBe
                    ParseResult.Success(
                        BinaryFunction.Multiply(
                            left = ValueHolder.WholeNumber(17),
                            right = ValueHolder.WholeNumber(5),
                        ),
                    )
            }
        }

        describe("Divide") {

            it("=17 / 5") {
                parser("=17 / 5") shouldBe
                    ParseResult.Success(
                        BinaryFunction.Divide(
                            left = ValueHolder.WholeNumber(17),
                            right = ValueHolder.WholeNumber(5),
                        ),
                    )
            }
        }
    }

    describe("Named Function") {

        it("=pow(2, 8)") {
            parser("=pow(2, 8)") shouldBe
                ParseResult.Success(
                    NamedFunction.Power(
                        base = ValueHolder.WholeNumber(2),
                        exponent = ValueHolder.WholeNumber(8),
                    ),
                )
        }

        it("=pow(A3 + 1, 8)") {
            parser("=pow(A3 + 1, 8)") shouldBe
                ParseResult.Success(
                    NamedFunction.Power(
                        base =
                            BinaryFunction.Plus(
                                left = ValueHolder.Reference("A3".toCellId()),
                                right = ValueHolder.WholeNumber(1),
                            ),
                        exponent = ValueHolder.WholeNumber(8),
                    ),
                )
        }
    }

    describe("Combination") {

        it("=-17 + -5") {
            parser("=-17 + -5") shouldBe
                ParseResult.Success(
                    BinaryFunction.Plus(
                        left =
                            UnaryFunction.Minus(
                                value = ValueHolder.WholeNumber(17),
                            ),
                        right =
                            UnaryFunction.Minus(
                                value = ValueHolder.WholeNumber(5),
                            ),
                    ),
                )
        }

        it("=-17 + -0.5") {
            parser("=-17 + -0.5") shouldBe
                ParseResult.Success(
                    BinaryFunction.Plus(
                        left =
                            UnaryFunction.Minus(
                                value = ValueHolder.WholeNumber(17),
                            ),
                        right =
                            UnaryFunction.Minus(
                                value = ValueHolder.DecimalNumber(0.5),
                            ),
                    ),
                )
        }

        it("=-17 + ---5") {
            parser("=-17 + ---5") shouldBe
                ParseResult.Success(
                    BinaryFunction.Plus(
                        left =
                            UnaryFunction.Minus(
                                value = ValueHolder.WholeNumber(17),
                            ),
                        right =
                            UnaryFunction.Minus(
                                value =
                                    UnaryFunction.Minus(
                                        value =
                                            UnaryFunction.Minus(
                                                value = ValueHolder.WholeNumber(5),
                                            ),
                                    ),
                            ),
                    ),
                )
        }

        it("=17 + -A3 * 5") {
            parser("=17 + -A3 * 5") shouldBe
                ParseResult.Success(
                    BinaryFunction.Multiply(
                        left =
                            BinaryFunction.Plus(
                                left = ValueHolder.WholeNumber(17),
                                right =
                                    UnaryFunction.Minus(
                                        value = ValueHolder.Reference("A3".toCellId()),
                                    ),
                            ),
                        right = ValueHolder.WholeNumber(5),
                    ),
                )
        }

        it("=17 + A3 * 5") {
            parser("=17 + A3 * 5") shouldBe
                ParseResult.Success(
                    BinaryFunction.Multiply(
                        left =
                            BinaryFunction.Plus(
                                left = ValueHolder.WholeNumber(17),
                                right = ValueHolder.Reference("A3".toCellId()),
                            ),
                        right = ValueHolder.WholeNumber(5),
                    ),
                )
        }
    }

    describe("Brackets") {

        it("=(17 + A3) * 5") {
            parser("=(17 + A3) * 5") shouldBe
                ParseResult.Success(
                    BinaryFunction.Multiply(
                        left =
                            BinaryFunction.Plus(
                                left = ValueHolder.WholeNumber(17),
                                right = ValueHolder.Reference("A3".toCellId()),
                            ),
                        right = ValueHolder.WholeNumber(5),
                    ),
                )
        }

        it("=17 + (A3 * 5)") {
            parser("=17 + (A3 * 5") shouldBe
                ParseResult.Success(
                    BinaryFunction.Multiply(
                        left =
                            BinaryFunction.Plus(
                                left = ValueHolder.WholeNumber(17),
                                right = ValueHolder.Reference("A3".toCellId()),
                            ),
                        right = ValueHolder.WholeNumber(5),
                    ),
                )
        }
    }
})
