package com.excel.calculator.api

@JvmInline
value class ColumnId(
    val value: Int,
) {
    companion object {
        private val ColumnLetters = ('A'..'Z').map { it.toString() }

        fun Int.toColumnId() = ColumnId(this)

        fun String.toColumnId(): ColumnId {
            val columnLetter = uppercase()
            val index = ColumnLetters.indexOf(columnLetter)
            return ColumnId(index)
        }
    }

    fun toColumnLetter(): String = ColumnLetters[value]
}
