package com.excel.calculator.api

@JvmInline
value class RowId(
    val value: Int,
) {
    companion object {
        fun Int.toRowId() = RowId(this)

        fun String.toRowId() = RowId(toInt() - 1)
    }

    fun toRowNumber(): String = (value + 1).toString()
}
