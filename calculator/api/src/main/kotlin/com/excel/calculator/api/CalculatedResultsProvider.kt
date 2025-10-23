package com.excel.calculator.api

interface CalculatedResultsProvider {

    operator fun get(key: CellId): CalculatedResult?
}
