package com.excel.ui.viewmodel

import app.cash.turbine.test
import com.excel.calculator.api.BinaryFunction
import com.excel.calculator.api.CalculatedResult.Companion.toCalculatedResult
import com.excel.calculator.api.CalculatedResultsProvider
import com.excel.calculator.api.CellId.Companion.toCellId
import io.kotest.core.spec.style.DescribeSpec
import com.excel.calculator.api.Evaluator
import com.excel.calculator.api.ParseResult
import com.excel.calculator.api.Parser
import com.excel.calculator.api.ValueHolder
import com.excel.ui.core.Dispatchers
import com.excel.calculator.api.ColumnId.Companion.toColumnId
import com.excel.calculator.api.EmptyNode
import com.excel.calculator.api.EvaluatorResult
import com.excel.ui.viewmodel.Expression.Companion.toExpression
import com.excel.calculator.api.RowId.Companion.toRowId
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

internal class ViewModelTest : DescribeSpec( {

    val mockCalculatedResultsProvider = mockk<CalculatedResultsProvider>()
    val mockEvaluator = mockk<Evaluator>(relaxUnitFun = true)
    val mockParser = mockk<Parser>()

    fun createViewModel() = ExcelViewModel(
        calculatedResultsProvider = mockCalculatedResultsProvider,
        dispatchers = Dispatchers,
        evaluator = mockEvaluator,
        parser = mockParser,
    )

    fun createNode() = BinaryFunction.Plus(
        left = ValueHolder.WholeNumber(1),
        right = ValueHolder.WholeNumber(1),
    )

    describe("onUpdate") {

        it("success") {
            val viewModel = createViewModel()
            val node = createNode()
            val cellId = "F7".toCellId()

            every { mockParser("1 + 1") } returns ParseResult.Success(node)
            every { mockEvaluator(cellId, node) } returns EvaluatorResult.Success(updatedCells = setOf(cellId))
            every { mockCalculatedResultsProvider[cellId] } returns 2.toCalculatedResult()

            viewModel.updates.test {
                viewModel.onUpdate(
                    rowId = 6.toRowId(),
                    columnId = 5.toColumnId(),
                    expression = "1 + 1".toExpression(),
                )
                awaitItem() shouldBe CellUpdate(
                    cellIds = setOf(cellId),
                )

                viewModel.getCellValue(
                    rowId = 6.toRowId(),
                    columnId = 5.toColumnId(),
                ) shouldBe CellValue.Success(
                    expression = "1 + 1".toExpression(),
                    result = 2.toCalculatedResult(),
                )
                cancel()
            }
        }


        it("empty") {
            val viewModel = createViewModel()
            val cellId = "F7".toCellId()

            every { mockParser("") } returns ParseResult.Empty
            every { mockEvaluator(cellId, EmptyNode) } returns EvaluatorResult.Success(updatedCells = setOf(cellId))

            viewModel.updates.test {
                viewModel.onUpdate(
                    rowId = 6.toRowId(),
                    columnId = 5.toColumnId(),
                    expression = Expression.EMPTY,
                )
                awaitItem() shouldBe CellUpdate(
                    cellIds = setOf(cellId),
                )

                viewModel.getCellValue(
                    rowId = 6.toRowId(),
                    columnId = 5.toColumnId(),
                ) shouldBe null

                cancel()
            }
        }

        it("failure") {
            val viewModel = createViewModel()
            val cellId = "F7".toCellId()

            every { mockParser("1 + 1") } returns ParseResult.Failure("error message")

            viewModel.updates.test {
                viewModel.onUpdate(
                    rowId = 6.toRowId(),
                    columnId = 5.toColumnId(),
                    expression = "1 + 1".toExpression(),
                )
                awaitItem() shouldBe CellUpdate(
                    cellIds = setOf(cellId),
                )

                viewModel.getCellValue(
                    rowId = 6.toRowId(),
                    columnId = 5.toColumnId(),
                ) shouldBe CellValue.Error(
                    expression = "1 + 1".toExpression(),
                    message = "error message",
                )
                cancel()
            }
        }

        it("cycle detected") {
            val viewModel = createViewModel()
            val node = createNode()
            val cellId = "F7".toCellId()

            every { mockParser("1 + 1") } returns ParseResult.Success(node)
            every { mockEvaluator(cellId, node) } returns EvaluatorResult.Failure(message = "Cycle detected")

            viewModel.updates.test {
                viewModel.onUpdate(
                    rowId = 6.toRowId(),
                    columnId = 5.toColumnId(),
                    expression = "1 + 1".toExpression(),
                )
                awaitItem() shouldBe CellUpdate(
                    cellIds = setOf(cellId),
                )

                viewModel.getCellValue(
                    rowId = 6.toRowId(),
                    columnId = 5.toColumnId(),
                ) shouldBe CellValue.Error(
                    expression = "1 + 1".toExpression(),
                    message = "Cycle detected",
                )
                cancel()
            }
        }
    }
})
