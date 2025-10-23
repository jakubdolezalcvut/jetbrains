package com.excel.ui.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class ViewModel(
    dispatchers: Dispatchers,
) {
    protected val viewModelScope = CoroutineScope(SupervisorJob() + dispatchers.Default)

    fun onClear() {
        viewModelScope.cancel()
    }
}
