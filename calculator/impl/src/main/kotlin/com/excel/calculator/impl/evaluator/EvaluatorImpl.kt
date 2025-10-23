package com.excel.calculator.impl.evaluator

import com.excel.calculator.api.BinaryFunction
import com.excel.calculator.api.CalculatedResult
import com.excel.calculator.api.CalculatedResult.Companion.toCalculatedResult
import com.excel.calculator.api.CellId
import com.excel.calculator.api.EmptyNode
import com.excel.calculator.api.Evaluator
import com.excel.calculator.api.EvaluatorResult
import com.excel.calculator.api.NamedFunction
import com.excel.calculator.api.Node
import com.excel.calculator.api.TextNode
import com.excel.calculator.api.UnaryFunction
import com.excel.calculator.api.ValueHolder

internal class EvaluatorImpl(
    private val calculatedResultsStore: CalculatedResultsStore,
    private val dependencyPool: DependencyPool,
    private val nodePool: NodePool,
) : Evaluator {

    override operator fun invoke(
        cellId: CellId,
        node: Node,
    ): EvaluatorResult {
        if (node is EmptyNode || node is TextNode) {
            updateNodeWithoutEvaluation(cellId)
            return EvaluatorResult.Success(updatedCells = setOf(cellId))
        }
        updateNodeWithEvaluation(cellId, node)

        if (dependencyPool.hasCycle(cellId)) {
            return EvaluatorResult.Failure(message = "Cycle detected")
        }
        updateObservingNodes(cellId)

        val updatedCells = buildSet {
            add(cellId)
            addAll(dependencyPool.getObservingCells(observedId = cellId))
        }
        return EvaluatorResult.Success(updatedCells = updatedCells)
    }

    private fun updateNodeWithoutEvaluation(cellId: CellId) {
        nodePool.remove(cellId)
        dependencyPool.removeObservingCell(observingId = cellId)
        calculatedResultsStore.remove(cellId)
    }

    private fun updateNodeWithEvaluation(cellId: CellId, node: Node) {
        nodePool[cellId] = node
        dependencyPool.removeObservingCell(observingId = cellId)
        calculatedResultsStore[cellId] = calculate(cellId, node)
    }

    private fun updateObservingNodes(changedCellId: CellId) {
        dependencyPool.getObservingCells(observedId = changedCellId).forEach { observingCellId ->
            val observingNode = nodePool[observingCellId] ?: return@forEach
            calculatedResultsStore[observingCellId] = calculate(observingCellId, observingNode)
            updateObservingNodes(observingCellId)
        }
    }

    private fun calculate(
        cellId: CellId,
        node: Node,
    ): CalculatedResult =
        when (node) {
            is BinaryFunction.Divide -> {
                calculate(cellId, node.left) / calculate(cellId, node.right)
            }
            is BinaryFunction.Minus -> {
                calculate(cellId, node.left) - calculate(cellId, node.right)
            }
            is BinaryFunction.Multiply -> {
                calculate(cellId, node.left) * calculate(cellId, node.right)
            }
            is BinaryFunction.Plus -> {
                calculate(cellId, node.left) + calculate(cellId, node.right)
            }
            is NamedFunction.Power -> {
                calculate(cellId, node.base).pow(calculate(cellId, node.exponent))
            }
            is TextNode, is EmptyNode -> {
                CalculatedResult.ZERO
            }
            is UnaryFunction.Minus -> {
                -calculate(cellId, node.value)
            }
            is ValueHolder.DecimalNumber -> {
                node.value.toCalculatedResult()
            }
            is ValueHolder.Reference -> {
                dependencyPool.add(node.cellId, cellId)
                calculatedResultsStore[node.cellId] ?: CalculatedResult.ZERO
            }
            is ValueHolder.WholeNumber -> {
                node.value.toCalculatedResult()
            }
        }
}
