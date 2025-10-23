package com.excel.calculator.api

import com.excel.calculator.api.ColumnId.Companion.toColumnId
import com.excel.calculator.api.RowId.Companion.toRowId

data class CellId(
    val rowId: RowId,
    val columnId: ColumnId,
) {
    companion object {
        private val Pattern = """^([a-zA-Z]+)(\d+)$""".toRegex()

        fun String.toCellId(): CellId {
            val match = Pattern.find(this)
                ?: throw IllegalArgumentException("Invalid CellId pattern $this")

            val rowText = match.groupValues[2]
            val columnText = match.groupValues[1]

            return CellId(
                rowId = rowText.toRowId(),
                columnId = columnText.toColumnId(),
            )
        }
    }

    val value: String
        get() {
            val rowNumber = rowId.toRowNumber()
            val columnLetter = columnId.toColumnLetter()
            return "$columnLetter$rowNumber"
        }
}
