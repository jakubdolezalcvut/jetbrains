package com.excel.ui.di

import com.excel.calculator.api.CalculatedResultsProvider
import com.excel.calculator.api.Evaluator
import com.excel.calculator.api.Parser
import io.kotest.core.spec.style.FunSpec
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify

@OptIn(KoinExperimentalAPI::class)
internal class UiModuleTest : KoinTest, FunSpec({

    test("verify Koin UI module") {
        uiModule.verify(
            extraTypes = listOf(
                CalculatedResultsProvider::class,
                Evaluator::class,
                Parser::class,
            ),
        )
    }
})
