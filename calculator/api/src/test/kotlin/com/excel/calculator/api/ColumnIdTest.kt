package com.excel.calculator.api

import com.excel.calculator.api.ColumnId.Companion.toColumnId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class ColumnIdTest : FunSpec({

    test("Int.toColumnId") {
        3.toColumnId() shouldBe ColumnId(3)
    }

    test("lowercase String.toColumnId") {
        "a".toColumnId() shouldBe ColumnId(0)
    }

    test("uppercase String.toColumnId") {
        "A".toColumnId() shouldBe ColumnId(0)
    }

    test("toColumnLetter") {
        "F".toColumnId().toColumnLetter() shouldBe "F"
    }
})
