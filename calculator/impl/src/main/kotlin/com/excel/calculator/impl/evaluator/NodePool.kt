package com.excel.calculator.impl.evaluator

import com.excel.calculator.api.CellId
import com.excel.calculator.api.Node

internal class NodePool {

    private val map = mutableMapOf<CellId, Node>()

    operator fun get(key: CellId): Node? = map[key]

    operator fun set(key: CellId, value: Node) {
        map[key] = value
    }

    fun remove(key: CellId) {
        map.remove(key)
    }
}
