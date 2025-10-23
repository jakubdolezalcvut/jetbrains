package com.excel.calculator.impl.evaluator

import com.excel.calculator.api.CalculatedResult
import com.excel.calculator.api.CalculatedResultsProvider
import com.excel.calculator.api.CellId

internal class CalculatedResultsStore : CalculatedResultsProvider {

    private val map = mutableMapOf<CellId, CalculatedResult>()

    override operator fun get(key: CellId): CalculatedResult? = map[key]

    operator fun set(key: CellId, value: CalculatedResult) {
        map[key] = value
    }

    fun remove(key: CellId) {
        map.remove(key)
    }
}
