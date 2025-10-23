package com.excel

import com.excel.calculator.impl.di.calculatorModule
import com.excel.ui.ExcelApp
import com.excel.ui.di.uiModule
import org.koin.core.context.startKoin
import javax.swing.SwingUtilities

fun main() {
    startKoin {
        modules(calculatorModule)
        modules(uiModule)
    }
    SwingUtilities.invokeLater { ExcelApp() }
}
