package asset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import tax.TaxableAmounts
import util.currentDate

class AssetRecTest : ShouldSpec({
    val assetName = "Asset Name"
    val person = "Person"
    val startBal = 10000.0

    val config = assetConfigFixture(assetName, person)
    val futureYear = 2090

    val gains = 1000.0
    val startUnrealized = 5000.0
    val rec = assetRecFixture(
        year = futureYear,
        assetConfig = config,
        startBal = startBal,
        gains = gains,
        startUnrealized = startUnrealized,
    )

    fun addTributions(copiedRec: AssetRec): AssetRec {
        copiedRec.tributions.add(
            AssetChange(
                name = "Contrib 1",
                amount = -3000.0,
                unrealized = -2000.0))
        copiedRec.tributions.add(
            AssetChange(
                name = "Contrib 2",
                amount = 4000.0))
        return copiedRec
    }

    val totalContrib = 1000.0
    val totalUnrealized = 3000.0

    should("calculates total gains") {
        rec.totalGains().shouldBe(gains)
    }

    should("calculates total contributions") {
        addTributions(rec.copy()).totalTributions().shouldBe(totalContrib)
    }

    should("calculates total unrelaized") {
        addTributions(rec.copy()).totalUnrealized().shouldBe(totalUnrealized)
    }

    should("captured gains is determined based on the year of the record") {
        val futureRec = rec.copy(year = futureYear)
        futureRec.capturedGains().shouldBe(0)

        val pastRec = rec.copy(year = 2000)
        pastRec.capturedGains().shouldBe(gains)

        val presentRec = rec.copy(year = currentDate.year)
        presentRec.capturedGains().shouldBeGreaterThan(0.0)
        presentRec.capturedGains().shouldBeLessThan(gains)
    }

    should("calculates the final balance") {
        val futureRec = addTributions(rec.copy(year = futureYear))
        futureRec.finalBalance().shouldBe(startBal + gains + totalContrib)

        val presentRec = addTributions(rec.copy(year = currentDate.year))
        presentRec.finalBalance().shouldBeLessThan(startBal + gains + totalContrib)
        presentRec.finalBalance().shouldBeGreaterThan(startBal + totalContrib)
    }

    should("generate income recs from req dist asset changes") {
        val amount = 3000.0
        val currRec = rec.copy()

        val taxable = TaxableAmounts(person = config.person, fed = amount, state = amount)
        val reqDistChange = AssetChange(
            name = "ReqDist", amount = -amount,
            taxable = taxable, isReqDist = true)
        currRec.tributions.add(reqDistChange)

        val otherTribution = AssetChange("NetSpend", 1000.0)
        currRec.tributions.add(otherTribution)

        val results = currRec.incomeRecs()
        results.shouldHaveSize(1)
        results[0].year.shouldBe(currRec.year)
        results[0].amount.shouldBe(amount)
        results[0].taxableIncome.shouldBe(taxable)
    }
})