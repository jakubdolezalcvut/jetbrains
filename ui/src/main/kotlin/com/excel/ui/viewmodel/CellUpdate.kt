package com.excel.ui.viewmodel

import com.excel.calculator.api.CellId

data class CellUpdate(
    val cellIds: Set<CellId>,
)
