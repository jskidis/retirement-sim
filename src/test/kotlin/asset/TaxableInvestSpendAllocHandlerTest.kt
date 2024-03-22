package asset

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class TaxableInvestSpendAllocHandlerTest : FunSpec({

    val year = 2020
    val stUnrealized = 2000.0
    val ltUnrealized = 10000.0

    val assetConfig = assetConfigFixture()
    val gain = AssetChange(name = "Gain", amount = 5000.0, unrealized = stUnrealized)
    val currYear = yearlyDetailFixture(year = year)

    val handler = TaxableInvestSpendAllocHandler()

    test("withdraw will use any unrealized gains from current year first") {
        val withdrawAmount = stUnrealized / 2
        val assetRec = AssetRec(year, assetConfig, startBal = 20000.0,
            startUnrealized = ltUnrealized, gains = gain
        )

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)

        result.shouldBe(withdrawAmount)
        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-withdrawAmount)
        assetRec.tributions[0].unrealized.shouldBe(-withdrawAmount)
        assetRec.tributions[0].taxable?.fed.shouldBe(withdrawAmount)
        assetRec.tributions[0].taxable?.fedLTG.shouldBe(0.0)
        assetRec.tributions[0].taxable?.state.shouldBe(withdrawAmount)
        assetRec.tributions[0].isCarryOver.shouldBeTrue()
        assetRec.totalUnrealized().shouldBe(ltUnrealized + stUnrealized - withdrawAmount)
    }

    test("withdraw will use all unrealized gains from current year then use up starting (lt) gains if withdraw amount is more than current year unrealized") {
        val withdrawAmount = stUnrealized + ltUnrealized / 2
        val assetRec = AssetRec(year, assetConfig, startBal = 20000.0,
            startUnrealized = ltUnrealized, gains = gain
        )

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)

        result.shouldBe(withdrawAmount)
        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-withdrawAmount)
        assetRec.tributions[0].unrealized.shouldBe(-withdrawAmount)
        assetRec.tributions[0].taxable?.fed.shouldBe(stUnrealized)
        assetRec.tributions[0].taxable?.fedLTG.shouldBe(withdrawAmount - stUnrealized)
        assetRec.tributions[0].taxable?.state.shouldBe(withdrawAmount)
        assetRec.tributions[0].isCarryOver.shouldBeTrue()
        assetRec.totalUnrealized().shouldBe(ltUnrealized + stUnrealized - withdrawAmount)
    }

    test("withdraw will use all unrealized gains (current year + previous if withdraw amount is more than total unrealized") {
        val withdrawAmount = stUnrealized + ltUnrealized + 100.0
        val assetRec = AssetRec(year, assetConfig, startBal = 20000.0,
            startUnrealized = ltUnrealized, gains = gain
        )

        val result = handler.withdraw(withdrawAmount, assetRec, currYear)

        result.shouldBe(withdrawAmount)
        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-withdrawAmount)
        assetRec.tributions[0].unrealized.shouldBe(-stUnrealized - ltUnrealized)
        assetRec.tributions[0].taxable?.fed.shouldBe(stUnrealized)
        assetRec.tributions[0].taxable?.fedLTG.shouldBe(ltUnrealized)
        assetRec.tributions[0].taxable?.state.shouldBe(stUnrealized + ltUnrealized)
        assetRec.tributions[0].isCarryOver.shouldBeTrue()
        assetRec.totalUnrealized().shouldBe(0.0)
    }
})
