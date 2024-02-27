package asset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import util.currentDate

class AssetRecTest : ShouldSpec({
    val assetName = "Asset Name"
    val person = "Person"
    val startBal = 10000.0

    val config = assetConfigFixture(assetName, person)
    val futureYear = 2090

    val gains = 1000.0
    val rec = assetRecFixture(
        year = futureYear,
        config, startBal,
        gains = gains)

    fun addTributions(copiedRec: AssetRec): AssetRec {
        copiedRec.tributions.add(SimpleAssetChange("Contrib 1", 3000.0))
        copiedRec.tributions.add(SimpleAssetChange("Contrib 2", 4000.0))
        return copiedRec
    }
    val totalContrib = 7000.0

    should("calculates total gains") {
        rec.totalGains().shouldBe(gains)
    }

    should("calculated total contributions") {
        addTributions(rec.copy()).totalTributions().shouldBe(totalContrib)
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

    should("calculated the final balance") {
        val futureRec = addTributions(rec.copy(year = futureYear))
        futureRec.finalBalance().shouldBe(startBal + gains + totalContrib)

        val presentRec = addTributions(rec.copy(year = currentDate.year))
        presentRec.finalBalance().shouldBeLessThan(startBal + gains + totalContrib)
        presentRec.finalBalance().shouldBeGreaterThan(startBal + totalContrib)
    }
})