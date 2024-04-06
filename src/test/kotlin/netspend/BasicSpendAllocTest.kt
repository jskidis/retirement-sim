package netspend

import Amount
import asset.AssetChange
import asset.assetRecFixture
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class BasicSpendAllocTest : FunSpec({

    val startBalance: Amount = 10000.0
    val gainAmount: Amount = 1000.0
    val balBeforeWD: Amount = startBalance + gainAmount

    val currYear = yearlyDetailFixture(year = currentDate.year + 1)

    val handler = BasicSpendAlloc()

    test("will withdraw full amount requested if amount request is less than asset balance") {
        val assetRec = assetRecFixture(
            startBal = startBalance,
            gains = AssetChange("Asset", gainAmount)
        )
        val withdrawAmount = balBeforeWD / 2

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(withdrawAmount)

        assetRec.finalBalance().shouldBe(balBeforeWD - withdrawAmount)
        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-withdrawAmount)
    }

    test("will withdraw year end (full) balance if amount request is greater than asset balance") {
        val assetRec = assetRecFixture(
            startBal = startBalance,
            gains = AssetChange("Asset", gainAmount))
        val withdrawAmount = balBeforeWD * 2

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)
        result.shouldBe(balBeforeWD)

        assetRec.finalBalance().shouldBe(0.0)
        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-balBeforeWD)
    }

    test("will deposit full amount requested") {
        val assetRec = assetRecFixture(
            startBal = startBalance,
            gains = AssetChange("Asset", gainAmount)
        )
        val depositAmount = balBeforeWD / 2

        val result = handler.deposit(depositAmount, assetRec, currYear)
        result.shouldBe(depositAmount)

        assetRec.finalBalance().shouldBe(balBeforeWD + depositAmount)
        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(depositAmount)
    }
})
