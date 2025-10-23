package com.excel.ui.viewmodel

@JvmInline
value class Expression(
    val value: String,
) {
    companion object {
        val EMPTY = Expression(value = "")

        fun String.toExpression() = Expression(this)
    }
}
