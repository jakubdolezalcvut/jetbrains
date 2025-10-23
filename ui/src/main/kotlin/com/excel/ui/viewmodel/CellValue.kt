package com.excel.ui.viewmodel

import com.excel.calculator.api.CalculatedResult

sealed interface CellValue {

    val expression: Expression

    data class Success(override val expression: Expression, val result: CalculatedResult?) : CellValue
    data class Error(override val expression: Expression, val message: String) : CellValue
}
