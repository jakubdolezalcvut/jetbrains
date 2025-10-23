package com.excel.calculator.api

import com.excel.calculator.api.CellId.Companion.toCellId
import com.excel.calculator.api.ColumnId.Companion.toColumnId
import com.excel.calculator.api.RowId.Companion.toRowId
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class CellIdTest : FunSpec({

    test("lowercase String.toCellId") {
        "a3".toCellId() shouldBe CellId(
            rowId = 2.toRowId(),
            columnId = 0.toColumnId(),
        )
    }

    test("uppercase String.toCellId") {
        "A3".toCellId() shouldBe CellId(
            rowId = 2.toRowId(),
            columnId = 0.toColumnId(),
        )
    }

    test("invalid String.toCellId") {
        shouldThrow<IllegalArgumentException> {
            "3A".toCellId()
        }
    }

    test("value") {
        "F3".toCellId().value shouldBe "F3"
    }
})
