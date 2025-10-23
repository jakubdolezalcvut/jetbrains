package com.excel.ui

import com.excel.ui.viewmodel.CellValue
import java.awt.Color
import java.awt.Component
import java.util.Locale
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

internal class FadingCellRenderer(
    locale: Locale = Locale.getDefault(),
) : DefaultTableCellRenderer() {

    private val formatter = CalculatedResultFormatter(locale)

    private val fadeDuration = 500L // ms

    override fun getTableCellRendererComponent(
        table: JTable,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

        when (value) {
            null -> {
               onEmpty()
            }
            is CellValue.Success -> {
                onSuccess(value)
            }
            is CellValue.Error -> {
                onError(value)
            }
            else -> throw IllegalArgumentException("Unrecognized value $value")
        }
        return component
    }

    private fun onEmpty() {
        foreground = Color.BLACK
    }

    private fun onSuccess(success: CellValue.Success) {
        text = with(formatter) { success.result?.format() } ?: success.expression.value
        foreground = Color.BLACK
    }

    private fun onError(error: CellValue.Error) {
        text = error.message
        foreground = Color.RED
    }
}
