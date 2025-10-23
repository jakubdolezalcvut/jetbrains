package com.excel.calculator.api

import com.excel.calculator.api.RowId.Companion.toRowId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class RowIdTest : FunSpec({

    test("Int.toRowId") {
        3.toRowId() shouldBe RowId(3)
    }

    test("String.toRowId") {
        "3".toRowId() shouldBe RowId(2)
    }

    test("toRowNumber") {
        "3".toRowId().toRowNumber() shouldBe "3"
    }
})
