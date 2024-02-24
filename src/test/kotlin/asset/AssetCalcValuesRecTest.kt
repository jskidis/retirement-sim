package asset

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
    val currYearRec = yearlyDetailFixture(year = 2090) // avoid partial year stuff until needed

    val gains = 1000.0
    val rec = assetRecFixture(config, startBal, gains = gains)

    rec.tributions.add(SimpleAssetChange("Contrib 1", 3000.0))
    rec.tributions.add(SimpleAssetChange("Contrib 2", 4000.0))
    val totalContrib = rec.tributions[0].totalAmount() + rec.tributions[1].totalAmount()

    should("create returns a new object with the total gains field calculated") {
        val result = AssetCalcValuesRec.create(rec, currYearRec)
        result.totalGains.shouldBe(gains)
    }

    should("create returns a new object with the total contributions field calculated") {
        val result = AssetCalcValuesRec.create(rec, currYearRec)
        result.totalTributions.shouldBe(totalContrib)
    }

    should("create determines captured gains based on the year of yearly detail record") {
        val yearFuture = currYearRec.copy(year = 2090)
        val resultFuture = AssetCalcValuesRec.create(rec, yearFuture)
        resultFuture.capturedGains.shouldBe(0)

        val yearPast = currYearRec.copy(year = 2000)
        val resultPast = AssetCalcValuesRec.create(rec, yearPast)
        resultPast.capturedGains.shouldBe(gains)

        val yearPresent = currYearRec.copy(year = currentDate.year)
        val resultPresent = AssetCalcValuesRec.create(rec, yearPresent)
        resultPresent.capturedGains.shouldBeGreaterThan(0.0)
        resultPresent.capturedGains.shouldBeLessThan(gains)
    }

    should("create returns a new object with the final balance calculated") {
        val resultFuture = AssetCalcValuesRec.create(rec, currYearRec)
        resultFuture.finalBal.shouldBe(startBal + gains + totalContrib)

        val yearPresent = currYearRec.copy(year = currentDate.year)
        val resultPresent = AssetCalcValuesRec.create(rec, yearPresent)
        resultPresent.finalBal.shouldBeLessThan(startBal + gains + totalContrib)
        resultPresent.finalBal.shouldBeGreaterThan(startBal + totalContrib)
    }
})