package com.excel.ui

import com.excel.calculator.api.CalculatedResult
import java.text.NumberFormat
import java.util.Locale

internal class CalculatedResultFormatter(
    locale: Locale,
) {
    private val format = NumberFormat.getNumberInstance(locale)

    fun CalculatedResult.format(): String =
        format.format(this.value)
}
