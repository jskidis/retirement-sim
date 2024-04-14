package netspend

import asset.AssetChange
import asset.assetRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class TaxableInvestSpendAllocHandlerTest : ShouldSpec({

    val year = currentDate.year + 1
    val stUnrealized = 2000.0
    val ltUnrealized = 10000.0

    val gain = AssetChange(name = "Gain", amount = 5000.0, unrealized = stUnrealized)
    val currYear = yearlyDetailFixture(year = year)


    should("withdraw will use any unrealized gains from current year first") {
        val withdrawAmount = stUnrealized / 2
        val assetRec = assetRecFixture(year, startBal = 20000.0,
            startUnrealized = ltUnrealized, gains = gain
        )

        val handler = TaxableInvestSpendAllocHandler()
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

    should("withdraw will use all unrealized gains from current year then use up starting (lt) gains if withdraw amount is more than current year unrealized") {
        val withdrawAmount = stUnrealized + ltUnrealized / 2
        val assetRec = assetRecFixture(year, startBal = 20000.0,
            startUnrealized = ltUnrealized, gains = gain
        )

        val handler = TaxableInvestSpendAllocHandler()
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

    should("withdraw will use all unrealized gains (current year + previous if withdraw amount is more than total unrealized") {
        val withdrawAmount = stUnrealized + ltUnrealized + 100.0
        val assetRec = assetRecFixture(year, startBal = 20000.0,
            startUnrealized = ltUnrealized, gains = gain
        )

        val handler = TaxableInvestSpendAllocHandler()
        val result = handler.withdraw(withdrawAmount, assetRec, currYear)

        result.shouldBe(withdrawAmount)
        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-withdrawAmount)
        assetRec.tributions[0].unrealized.shouldBe(-stUnrealized - ltUnrealized)
        assetRec.tributions[0].taxable?.fed.shouldBe(stUnrealized)
        assetRec.tributions[0].taxable?.fedLTG.shouldBe(ltUnrealized)
        assetRec.tributions[0].taxable?.state.shouldBe(stUnrealized + ltUnrealized)
        assetRec.tributions[0].isCarryOver.shouldBeTrue()
        assetRec.totalUnrealized().shouldBeZero()
    }

    should("not having any long term unrealized if startUnrealized is 0") {
        val withdrawAmount = stUnrealized / 2
        val assetRec = assetRecFixture(year, startBal = 20000.0,
            startUnrealized = 0.0, gains = gain
        )

        val handler = TaxableInvestSpendAllocHandler()
        val result = handler.withdraw(withdrawAmount, assetRec, currYear)

        result.shouldBe(withdrawAmount)
        assetRec.tributions.shouldHaveSize(1)
        assetRec.tributions[0].amount.shouldBe(-withdrawAmount)
        assetRec.tributions[0].unrealized.shouldBe(-withdrawAmount)
        assetRec.tributions[0].taxable?.fed.shouldBe(withdrawAmount)
        assetRec.tributions[0].taxable?.fedLTG?.shouldBeZero()
        assetRec.tributions[0].taxable?.state.shouldBe(withdrawAmount)
        assetRec.tributions[0].isCarryOver.shouldBeTrue()
        assetRec.totalUnrealized().shouldBe(stUnrealized - withdrawAmount)
    }

    should("not allow withdraws below minimum balance") {
        val gainAmt = 100.0
        val smallGain = AssetChange(name = "Gain", amount = gainAmt)

        val startBal = 2000.0
        val assetRec = assetRecFixture(year, startBal = startBal, gains = smallGain)

        val minBalance = 1000.0
        val handler = TaxableInvestSpendAllocHandler(minBalance)

        val expectedWithdraw = startBal + gainAmt - minBalance
        handler.withdraw(100000.0, assetRec, currYear).shouldBe(expectedWithdraw)
    }
})
