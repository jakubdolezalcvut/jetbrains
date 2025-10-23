package com.excel.ui.di

import com.excel.ui.core.Dispatchers
import com.excel.ui.viewmodel.ExcelViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val uiModule = module {
    single { Dispatchers }
    singleOf(::ExcelViewModel)
}
