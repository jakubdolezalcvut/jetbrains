package com.excel.calculator.api

sealed interface EvaluatorResult {

    data class Success(val updatedCells: Set<CellId>) : EvaluatorResult
    data class Failure(val message: String) : EvaluatorResult
}
