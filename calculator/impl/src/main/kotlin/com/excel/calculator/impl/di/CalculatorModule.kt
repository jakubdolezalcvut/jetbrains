package com.excel.calculator.impl.di

import com.excel.calculator.api.CalculatedResultsProvider
import com.excel.calculator.api.Evaluator
import com.excel.calculator.api.Parser
import com.excel.calculator.impl.evaluator.CalculatedResultsStore
import com.excel.calculator.impl.evaluator.DependencyPool
import com.excel.calculator.impl.evaluator.EvaluatorImpl
import com.excel.calculator.impl.evaluator.NodePool
import com.excel.calculator.impl.parser.ParserImpl
import com.excel.calculator.impl.parser.Tokenizer
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val calculatorModule = module {
    singleOf(::CalculatedResultsStore) bind CalculatedResultsProvider::class
    singleOf(::DependencyPool)
    singleOf(::NodePool)

    factoryOf(::EvaluatorImpl) bind Evaluator::class
    factoryOf(::ParserImpl) bind Parser::class
    factoryOf(::Tokenizer)
}
