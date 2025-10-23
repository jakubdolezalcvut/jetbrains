package com.excel.calculator.impl.evaluator

import com.excel.calculator.api.CellId

internal class DependencyPool {

    private val graph = mutableMapOf<CellId, MutableSet<CellId>>()

    fun getObservingCells(observedId: CellId): Set<CellId> =
        graph[observedId] ?: emptySet()

    fun add(
        observedId: CellId,
        observingId: CellId,
    ) {
        val set = graph[observedId]

        if (set == null) {
            graph[observedId] = mutableSetOf(observingId)
        } else {
            set += observingId
        }
    }

    fun removeObservingCell(
        observingId: CellId,
    ) {
        val observedIdsToRemove = mutableSetOf<CellId>()

        graph.forEach { (observedId, observingIds) ->
            observingIds.remove(observingId)
            if (observingIds.isEmpty()) {
                observedIdsToRemove += observedId
            }
        }
        observedIdsToRemove.forEach(graph::remove)
    }

    /**
     * https://medium.com/@chetanshingare2991/detecting-cycles-and-ordering-dependencies-graph-algorithms-in-kotlin-a3807cf8a57c
     * https://masterinkotlin.com/detect-cycle-in-directed-graph-in-kotlin/
     */
    fun hasCycle(
        cellId: CellId,
    ): Boolean =
        hasCycle(
            visited = mutableSetOf(),
            recursionStack = mutableSetOf(),
            current = cellId,
        )

    private fun hasCycle(
        visited: MutableSet<CellId>,
        recursionStack: MutableSet<CellId>,
        current: CellId,
    ): Boolean {
        visited += current
        recursionStack += current

        graph[current].orEmpty().forEach { neighbor ->
            if ((neighbor !in visited) && hasCycle(visited, recursionStack, neighbor)) {
                return true
            } else if (neighbor in recursionStack) {
                return true
            }
        }
        recursionStack -= current
        return false
    }
}
