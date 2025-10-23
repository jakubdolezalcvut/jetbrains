package com.excel.ui

import com.excel.ui.viewmodel.CellValue
import java.awt.Component
import javax.swing.DefaultCellEditor
import javax.swing.JTable
import javax.swing.JTextField

internal class ExpressionCellEditor : DefaultCellEditor(JTextField()) {

    override fun getTableCellEditorComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        val textField = editorComponent as JTextField

        textField.text = when (value) {
            null -> {
                ""
            }
            is CellValue.Success, is CellValue.Error -> {
                value.expression.value
            }
            else -> throw IllegalArgumentException("Unrecognized value $value")
        }
        return textField
    }
}
