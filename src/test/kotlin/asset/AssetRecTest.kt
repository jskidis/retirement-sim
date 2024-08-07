package asset

import RecIdentifier
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import util.currentDate

class AssetRecTest : ShouldSpec({
    val assetName = "Asset Name"
    val person = "Person"
    val startBal = 10000.0

    val ident = RecIdentifier(assetName, person)
    val currentYear = currentDate.year
    val futureYear = currentYear + 10

    val gainAmont = 1000.0
    val startUnrealized = 5000.0
    val rec = assetRecFixture(
        year = futureYear,
        ident = ident,
        startBal = startBal,
        gains = AssetChange("Asset", gainAmont),
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
        rec.totalGains().shouldBe(gainAmont)
    }

    should("calculates total contributions") {
        addTributions(rec.copy()).totalTributions().shouldBe(totalContrib)
    }

    should("calculates total unrelaized") {
        addTributions(rec.copy()).totalUnrealized().shouldBe(totalUnrealized)
    }

    should("captured gains is determined based on the year of the record") {
        val futureRec = rec.copy(year = futureYear)
        futureRec.proratedGains().shouldBe(0)

        val presentRec = rec.copy(year = currentYear)
        presentRec.proratedGains().shouldBeGreaterThan(0.0)
        presentRec.proratedGains().shouldBeLessThan(gainAmont)
    }

    should("calculates the final balance") {
        val futureRec = addTributions(rec.copy(year = futureYear))
        futureRec.finalBalance().shouldBe(startBal + gainAmont + totalContrib)

        val presentRec = addTributions(rec.copy(year = futureYear))
        presentRec.finalBalance().shouldBe(startBal + gainAmont + totalContrib)
    }
})