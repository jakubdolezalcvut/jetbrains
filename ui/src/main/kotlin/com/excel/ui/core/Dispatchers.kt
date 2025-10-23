package com.excel.ui.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.swing.Swing

object Dispatchers {

    /**
     * Should be used for suspending Coroutine calls
     */
    val Default: CoroutineDispatcher = Dispatchers.Default

    /**
     * Should be used for blocking Java calls
     */
    val IO: CoroutineDispatcher = Dispatchers.IO

    /**
     * Delegates to the Swing EDT (Event Dispatch Thread)
     * Same as Dispatchers.Main if kotlinx-coroutines-swing is added as a dependency
     * Name [Swing] added explicitly for clarity
     */
    val Swing: CoroutineDispatcher = Dispatchers.Swing
}
