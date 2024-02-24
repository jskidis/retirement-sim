package asset

import Amount
import Name
import currentDate
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class SimpleAssetProgressionTest : ShouldSpec({

    val assetName: Name = "Asset Name"
    val person: Name = "Person"
    val startBalance: Amount = 1000.0

    val tenPctReturn = PortfolAttribs(name="Ten Pct", mean = 0.10, stdDev = 0.01)
    val onePctReturn = PortfolAttribs(name="One Pct", mean = 0.01, stdDev = 0.001)

    val baseAssetConfig = assetConfigFixture(assetName, person)
    val prevAssetRec = assetRecFixture(
        assetConfig = baseAssetConfig, startBal = 0.0, endBal = startBalance
    )

    val prevYear = yearlyDetailFixture().copy(assets = listOf(prevAssetRec))

    should("determineNext returns asset rec same gain all years)") {
        val attributeSet = listOf(
            YearlyAssetAttributes(2024, tenPctReturn)
        )
        val progression = SimpleAssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig.copy(attributesSet = attributeSet))

        val results = progression.determineNext(prevYear.copy(year = 2024))
        results.config.name.shouldBe(assetName)
        results.config.person.shouldBe(person)
        results.gains.name.shouldBe(tenPctReturn.name)
        results.gains.totalAmount().shouldBeWithinPercentageOf(
            startBalance * tenPctReturn.mean, .001)
    }

    should("determineNext returns asset rec for different years)") {
        val attributeSet = listOf(
            YearlyAssetAttributes(currentDate.year, tenPctReturn),
            YearlyAssetAttributes(2030, onePctReturn)
        )
        val progression = SimpleAssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig.copy(attributesSet = attributeSet))

        val results2024 = progression.determineNext(prevYear.copy(year = 2024))
        results2024.gains.name.shouldBe(tenPctReturn.name)
        results2024.gains.totalAmount().shouldBeWithinPercentageOf(
            startBalance * tenPctReturn.mean, .001)

        val results2034 = progression.determineNext(prevYear.copy(year = 2034))
        results2034.gains.name.shouldBe(onePctReturn.name)
        results2034.gains.totalAmount().shouldBeWithinPercentageOf(
            startBalance * onePctReturn.mean, .001)
    }

    should("determineNext uses current year if previous year not provided") {
        val attributeSet = listOf(
            YearlyAssetAttributes(currentDate.year, tenPctReturn),
            YearlyAssetAttributes(2040, onePctReturn)
        )
        val progression = SimpleAssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig.copy(attributesSet = attributeSet))

        val results = progression.determineNext(null)
        results.gains.name.shouldBe(tenPctReturn.name)
        results.gains.totalAmount().shouldBeWithinPercentageOf(
            startBalance * tenPctReturn.mean, .001)
    }

    should("determineNext uses start amount if asset rec not found in previous year") {
        val attributeSet = listOf(
            YearlyAssetAttributes(2024, tenPctReturn)
        )
        val progression = SimpleAssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig.copy(attributesSet = attributeSet))

        val prevYearMissingAsset = prevYear.copy(assets = listOf())
        val results = progression.determineNext(prevYearMissingAsset)
        results.gains.name.shouldBe(tenPctReturn.name)
        results.gains.totalAmount().shouldBeWithinPercentageOf(
            startBalance * tenPctReturn.mean, .001)
    }
})
