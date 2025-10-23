package com.excel.ui

import com.excel.calculator.api.ColumnId.Companion.toColumnId
import com.excel.ui.viewmodel.ExcelViewModel
import com.excel.calculator.api.RowId.Companion.toRowId
import com.excel.ui.viewmodel.Expression.Companion.toExpression
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.swing.table.AbstractTableModel

internal class CellObservingTableModel(
    uiScope: CoroutineScope,
    private val viewModel: ExcelViewModel,
) : AbstractTableModel() {

    private object Const {
        const val Rows = 1000
        const val Columns = 26
    }

    init {
        viewModel.updates
            .onEach { cellUpdate ->
                cellUpdate.cellIds.forEach { cellId ->
                    fireTableCellUpdated(cellId.rowId.value, cellId.columnId.value)
                }
            }
            .launchIn(uiScope)
    }

    override fun getRowCount(): Int = Const.Rows

    override fun getColumnCount(): Int = Const.Columns

    override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = true

    override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? =
        viewModel.getCellValue(
            rowId = rowIndex.toRowId(),
            columnId = columnIndex.toColumnId(),
        )

    override fun setValueAt(value: Any, rowIndex: Int, columnIndex: Int) {
        viewModel.onUpdate(
            rowId = rowIndex.toRowId(),
            columnId = columnIndex.toColumnId(),
            expression = value.toString().toExpression(),
        )
    }

    override fun getColumnName(column: Int): String =
        column.toColumnId().toColumnLetter()
}
