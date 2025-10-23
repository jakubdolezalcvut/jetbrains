package com.excel.calculator.impl.di

import io.kotest.core.spec.style.FunSpec
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
internal class CalculatorModuleTest : KoinTest, FunSpec({

    test("verify Koin calculator module") {
        calculatorModule.verify()
    }
})
