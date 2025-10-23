package com.excel.calculator.impl.evaluator

import com.excel.calculator.api.CalculatedResult.Companion.toCalculatedResult
import com.excel.calculator.api.CellId.Companion.toCellId
import com.excel.calculator.api.EmptyNode
import com.excel.calculator.api.EvaluatorResult
import com.excel.calculator.api.ValueHolder
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

internal class EvaluatorTest : DescribeSpec({

    val cellA3 = "A3".toCellId()
    val cellB7 = "B7".toCellId()
    val cellC1 = "C1".toCellId()

    fun createEvaluator(
        calculatedResultsStore: CalculatedResultsStore = CalculatedResultsStore(),
        dependencyPool: DependencyPool = DependencyPool(),
        nodePool: NodePool = NodePool(),
    ) = EvaluatorImpl(
        calculatedResultsStore = calculatedResultsStore,
        dependencyPool = dependencyPool,
        nodePool = nodePool,
    )

    describe("EmptyNode") {

        it("removes all references") {
            val calculatedResultsStore = CalculatedResultsStore()
            val dependencyPool = DependencyPool()
            val nodePool = NodePool()
            val evaluator = createEvaluator(calculatedResultsStore, dependencyPool, nodePool)

            val nodeA3 = ValueHolder.WholeNumber(17)
            val nodeB7 = ValueHolder.Reference(cellA3)

            evaluator(cellA3, nodeA3)
            evaluator(cellB7, nodeB7)

            evaluator(cellB7, EmptyNode) shouldBe EvaluatorResult.Success(
                updatedCells = setOf(cellB7),
            )
            nodePool[cellB7] shouldBe null
            calculatedResultsStore[cellB7] shouldBe null
            dependencyPool.getObservingCells(cellA3) shouldBe emptySet()
        }
    }

    describe("Cycle") {

        it("Two cells referencing each other") {
            val evaluator = createEvaluator()

            val nodeA3 = ValueHolder.Reference(cellB7)
            val nodeB7 = ValueHolder.Reference(cellA3)

            evaluator(cellA3, nodeA3)

            evaluator(cellB7, nodeB7) shouldBe EvaluatorResult.Failure(
                message = "Cycle detected",
            )
        }

        it("Cycle of three cells") {
            val evaluator = createEvaluator()

            val nodeA3 = ValueHolder.Reference(cellB7)
            val nodeB7 = ValueHolder.Reference(cellC1)
            val nodeC1 = ValueHolder.Reference(cellA3)

            evaluator(cellA3, nodeA3)
            evaluator(cellB7, nodeB7)
            evaluator(cellC1, nodeC1) shouldBe EvaluatorResult.Failure(
                message = "Cycle detected",
            )
        }
    }

    describe("Value Holder") {

        it("17") {
            val calculatedResultsStore = CalculatedResultsStore()
            val evaluator = createEvaluator(calculatedResultsStore)

            val node = ValueHolder.WholeNumber(17)

            evaluator(cellA3, node) shouldBe EvaluatorResult.Success(
                updatedCells = setOf(cellA3),
            )
            calculatedResultsStore[cellA3] shouldBe 17.toCalculatedResult()
        }
    }

    describe("Reference") {

        it("Direct") {
            val calculatedResultsStore = CalculatedResultsStore()
            val evaluator = createEvaluator(calculatedResultsStore)

            val nodeA3 = ValueHolder.WholeNumber(17)
            val nodeB7 = ValueHolder.Reference(cellA3)

            evaluator(cellA3, nodeA3)

            evaluator(cellB7, nodeB7) shouldBe EvaluatorResult.Success(
                updatedCells = setOf(cellB7),
            )
            calculatedResultsStore[cellA3] shouldBe 17.toCalculatedResult()
            calculatedResultsStore[cellB7] shouldBe 17.toCalculatedResult()
        }

        it("Transitive") {
            val calculatedResultsStore = CalculatedResultsStore()
            val evaluator = createEvaluator(calculatedResultsStore)

            val nodeA3 = ValueHolder.WholeNumber(17)
            val nodeB7 = ValueHolder.Reference(cellA3)
            val nodeC1 = ValueHolder.Reference(cellB7)

            evaluator(cellA3, nodeA3)
            evaluator(cellB7, nodeB7)

            evaluator(cellC1, nodeC1) shouldBe EvaluatorResult.Success(
                updatedCells = setOf(cellC1),
            )
            calculatedResultsStore[cellA3] shouldBe 17.toCalculatedResult()
            calculatedResultsStore[cellB7] shouldBe 17.toCalculatedResult()
            calculatedResultsStore[cellC1] shouldBe 17.toCalculatedResult()
        }

        it("Transitive Disconnected") {
            val calculatedResultsStore = CalculatedResultsStore()
            val evaluator = createEvaluator(calculatedResultsStore)

            val nodeA3 = ValueHolder.WholeNumber(17)
            val nodeB7 = ValueHolder.Reference(cellA3)
            val nodeC1 = ValueHolder.Reference(cellB7)
            val updatedNodeB7 = ValueHolder.WholeNumber(3)

            evaluator(cellA3, nodeA3)
            evaluator(cellB7, nodeB7)
            evaluator(cellC1, nodeC1)

            evaluator(cellB7, updatedNodeB7) shouldBe EvaluatorResult.Success(
                updatedCells = setOf(cellB7, cellC1),
            )
            calculatedResultsStore[cellA3] shouldBe 17.toCalculatedResult()
            calculatedResultsStore[cellB7] shouldBe 3.toCalculatedResult()
            calculatedResultsStore[cellC1] shouldBe 3.toCalculatedResult()
        }

        it("Transitive Updated") {
            val calculatedResultsStore = CalculatedResultsStore()
            val evaluator = createEvaluator(calculatedResultsStore)

            val nodeA3 = ValueHolder.WholeNumber(17)
            val nodeB7 = ValueHolder.Reference(cellA3)
            val nodeC1 = ValueHolder.Reference(cellB7)
            val updatedNodeB7 = ValueHolder.WholeNumber(3)
            val updatedNodeA3 = ValueHolder.WholeNumber(10)

            evaluator(cellA3, nodeA3)
            evaluator(cellB7, nodeB7)
            evaluator(cellC1, nodeC1)

            evaluator(cellB7, updatedNodeB7) shouldBe EvaluatorResult.Success(
                updatedCells = setOf(cellB7, cellC1),
            )
            evaluator(cellA3, updatedNodeA3) shouldBe EvaluatorResult.Success(
                updatedCells = setOf(cellA3),
            )
            calculatedResultsStore[cellA3] shouldBe 10.toCalculatedResult()
            calculatedResultsStore[cellB7] shouldBe 3.toCalculatedResult()
            calculatedResultsStore[cellC1] shouldBe 3.toCalculatedResult()
        }
    }
})
