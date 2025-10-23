package com.excel.ui.viewmodel

import com.excel.calculator.api.CalculatedResultsProvider
import com.excel.calculator.api.CellId
import com.excel.calculator.api.ColumnId
import com.excel.calculator.api.EmptyNode
import com.excel.calculator.api.Evaluator
import com.excel.calculator.api.EvaluatorResult
import com.excel.calculator.api.Node
import com.excel.calculator.api.ParseResult
import com.excel.calculator.api.Parser
import com.excel.calculator.api.RowId
import com.excel.ui.core.Dispatchers
import com.excel.ui.core.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class ExcelViewModel(
    private val calculatedResultsProvider: CalculatedResultsProvider,
    dispatchers: Dispatchers,
    private val evaluator: Evaluator,
    private val parser: Parser,
) : ViewModel(dispatchers) {

    private val cells: MutableMap<CellId, CellValue> = mutableMapOf()

    private val _updates = MutableSharedFlow<CellUpdate>(
        extraBufferCapacity = 16,
    )
    val updates: SharedFlow<CellUpdate> = _updates.asSharedFlow()

    fun getCellValue(rowId: RowId, columnId: ColumnId): CellValue? {
        val cellId = CellId(rowId, columnId)
        return cells[cellId]
    }

    fun onUpdate(rowId: RowId, columnId: ColumnId, expression: Expression) {
        viewModelScope.launch {
            val cellId = CellId(rowId, columnId)
            val parseResult = parser(expression.value)
            val updatedCells = evaluateCells(cellId, expression, parseResult)
            updateCells(updatedCells)
        }
    }

    private fun evaluateCells(
        cellId: CellId,
        expression: Expression,
        parseResult: ParseResult,
    ): Set<CellId> =
        when (parseResult) {
            is ParseResult.Empty -> {
                evaluateCells(cellId, expression, EmptyNode)
            }
            is ParseResult.Failure -> {
                cells[cellId] = CellValue.Error(
                    expression = expression,
                    message = parseResult.message,
                )
                setOf(cellId)
            }
            is ParseResult.Success -> {
                evaluateCells(cellId, expression, parseResult.node)
            }
        }

    private fun evaluateCells(
        cellId: CellId,
        expression: Expression,
        node: Node,
    ): Set<CellId> =
        when (val evaluatorResult = evaluator(cellId, node)) {
            is EvaluatorResult.Failure -> {
                cells[cellId] = CellValue.Error(
                    expression = expression,
                    message = evaluatorResult.message,
                )
                setOf(cellId)
            }
            is EvaluatorResult.Success -> {
                if (node == EmptyNode) {
                    cells.remove(cellId)
                } else {
                    cells[cellId] = CellValue.Success(
                        expression = expression,
                        result = calculatedResultsProvider[cellId],
                    )
                }
                evaluatorResult.updatedCells
            }
        }

    private suspend fun updateCells(
        updatedCells: Set<CellId>,
    ) {
        updatedCells.forEach { cellId ->
            val cell = cells[cellId] ?: return@forEach

            cells[cellId] = when (cell) {
                is CellValue.Error -> {
                    cell
                }
                is CellValue.Success -> {
                    cell.copy(
                        result = calculatedResultsProvider[cellId],
                    )
                }
            }
        }
        _updates.emit(
            value = CellUpdate(
                cellIds = updatedCells,
            )
        )
    }
}
