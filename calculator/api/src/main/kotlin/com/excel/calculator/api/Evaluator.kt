package com.excel.calculator.api

interface Evaluator {

    operator fun invoke(cellId: CellId, node: Node): EvaluatorResult
}
