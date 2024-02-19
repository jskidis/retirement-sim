package asset

import AssetGain
import AssetNetContribution
import assetConfigFixture
import assetRecFixture
import currentDate
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class AssetCalcValuesRecTest  : ShouldSpec({
    val assetName = "Asset Name"
    val person = "Person"
    val startBal = 10000.0

    val config = assetConfigFixture(assetName, person)
    val baseRec = assetRecFixture(config, startBal)
    val currYearRec = yearlyDetailFixture(year = 2090) // avoid partial year stuff until needed

    val gain1 = AssetGain("Gain 1", 1000.0)
    val gain2 = AssetGain("Gain 2", 2000.0)
    val totalGains = gain1.amount + gain2.amount

    val contrib1 = AssetNetContribution("Contrib 1", 3000.0)
    val contrib2 = AssetNetContribution("Contrib 2", 4000.0)
    val totalContrib = contrib1.amount + contrib2.amount

    val rec = baseRec.copy(
        gains = listOf(gain1, gain2),
        contributions = listOf(contrib1, contrib2))

    should("create returns a new object with the total gains field calculated") {
        val result = AssetCalcValuesRec.create(rec, currYearRec)
        result.totalGains.shouldBe(totalGains)
    }

    should("create returns a new object with the total contributions field calculated") {
        val result = AssetCalcValuesRec.create(rec, currYearRec)
        result.totalContributions.shouldBe(totalContrib)
    }

    should("create determines captured gains based on the year of yearly detail record") {
        val yearFuture = currYearRec.copy(year = 2090)
        val resultFuture = AssetCalcValuesRec.create(rec, yearFuture)
        resultFuture.capturedGains.shouldBe(0)

        val yearPast = currYearRec.copy(year = 2000)
        val resultPast = AssetCalcValuesRec.create(rec, yearPast)
        resultPast.capturedGains.shouldBe(totalGains)

        val yearPresent = currYearRec.copy(year = currentDate.year)
        val resultPresent = AssetCalcValuesRec.create(rec, yearPresent)
        resultPresent.capturedGains.shouldBeGreaterThan(0.0)
        resultPresent.capturedGains.shouldBeLessThan(totalGains)
    }

    should("create returns a new object with the final balance calculated") {
        val resultFuture = AssetCalcValuesRec.create(rec, currYearRec)
        resultFuture.finalBal.shouldBe(startBal + totalGains + totalContrib)

        val yearPresent = currYearRec.copy(year = currentDate.year)
        val resultPresent = AssetCalcValuesRec.create(rec, yearPresent)
        resultPresent.finalBal.shouldBeLessThan(startBal + totalGains + totalContrib)
        resultPresent.finalBal.shouldBeGreaterThan(startBal + totalContrib)
    }
})