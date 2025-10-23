package com.excel.calculator.api

sealed interface Node

data object EmptyNode : Node

data class TextNode(
    val value: String,
) : Node

sealed interface ValueHolder : Node {
    data class WholeNumber(
        val value: Long,
    ) : ValueHolder

    data class DecimalNumber(
        val value: Double,
    ) : ValueHolder

    data class Reference(
        val cellId: CellId,
    ) : ValueHolder
}

sealed interface UnaryFunction : Node {
    val value: Node

    data class Minus(
        override val value: Node,
    ) : UnaryFunction
}

sealed interface BinaryFunction : Node {
    val left: Node
    val right: Node

    data class Plus(
        override val left: Node,
        override val right: Node,
    ) : BinaryFunction

    data class Minus(
        override val left: Node,
        override val right: Node,
    ) : BinaryFunction

    data class Multiply(
        override val left: Node,
        override val right: Node,
    ) : BinaryFunction

    data class Divide(
        override val left: Node,
        override val right: Node,
    ) : BinaryFunction
}

sealed interface NamedFunction : Node {
    data class Power(
        val base: Node,
        val exponent: Node,
    ) : NamedFunction
}
