package asset

import Amount
import Name
import assetConfigFixture
import assetRecFixture
import currentDate
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import tax.NonWageTaxableProfile
import yearlyDetailFixture

class BasicAssetProgressionTest : ShouldSpec({
    val assetName: Name = "Asset Name"
    val person: Name = "Person"
    val startBalance: Amount = 1000.0

    val baseAssetConfig = assetConfigFixture(assetName, person)
    val prevAssetRec = assetRecFixture(
        assetConfig = baseAssetConfig, startBal = 0.0, endBal = startBalance
    )

    val tenPercentRORProvider = BasicAssetRORProvider(mean = 0.1, stdDev = 0.05)
    val onePercentRORProviders = BasicAssetRORProvider(mean = 0.01, stdDev = 0.01)

    val prevYear = yearlyDetailFixture()
    prevYear.assets.add(prevAssetRec)

    should("determineNext returns asset rec with single gain, single asset class composition(all years)") {
        val assetComposition = listOf(
            AssetComposition("10 Pct Return", 1.0, tenPercentRORProvider)
        )
        val assetCompMap = listOf(
            YearlyAssetComposition(2024, assetComposition)
        )
        val progression = BasicAssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig.copy(assetCompMap = assetCompMap))

        val results = progression.determineNext(prevYear.copy(year = 2024))
        results.config.name.shouldBe(assetName)
        results.config.person.shouldBe(person)
        results.gains.size.shouldBe(1)
        results.gains[0].name.shouldBe(assetComposition[0].name)
        results.gains[0].amount.shouldBeWithinPercentageOf(
            startBalance * tenPercentRORProvider.mean, .001)
    }

    should("determineNext returns asset rec with single gains, single asset class composition(but different for different years)") {
        val assetCompositionEarly = listOf(
            AssetComposition("10 Pct Return", 1.0, tenPercentRORProvider),
        )
        val assetCompositionLater = listOf(
            AssetComposition("1 Pct Return", 1.0, onePercentRORProviders),
        )
        val assetCompMap = listOf(
            YearlyAssetComposition(2024, assetCompositionEarly),
            YearlyAssetComposition(2030, assetCompositionLater)
        )
        val progression = BasicAssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig.copy(assetCompMap = assetCompMap))

        val results2024 = progression.determineNext(prevYear.copy(year = 2024))
        results2024.gains.size.shouldBe(1)
        results2024.gains[0].name.shouldBe(assetCompositionEarly[0].name)
        results2024.gains[0].amount.shouldBeWithinPercentageOf(
            startBalance * tenPercentRORProvider.mean, .001)

        val results2034 = progression.determineNext(prevYear.copy(year = 2034))
        results2034.gains.size.shouldBe(1)
        results2034.gains[0].name.shouldBe(assetCompositionLater[0].name)
        results2034.gains[0].amount.shouldBeWithinPercentageOf(
            startBalance * onePercentRORProviders.mean, .001)
    }

    should("determineNext returns asset rec with two gain, two asset class composition(all years)") {
        val assetComposition = ArrayList(
            listOf(
                AssetComposition("10 Pct Return", 0.8, tenPercentRORProvider),
                AssetComposition("1 Pct Return", 0.2, onePercentRORProviders)
            ))
        val assetCompMap = listOf(
            YearlyAssetComposition(2024, assetComposition)
        )
        val progression = BasicAssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig.copy(assetCompMap = assetCompMap))

        val results = progression.determineNext(prevYear.copy(year = 2024))
        results.gains.size.shouldBe(2)

        results.gains[0].name.shouldBe(assetComposition[0].name)
        results.gains[0].amount.shouldBeWithinPercentageOf(
            startBalance * tenPercentRORProvider.mean * assetComposition[0].pct, .001)

        results.gains[1].name.shouldBe(assetComposition[1].name)
        results.gains[1].amount.shouldBeWithinPercentageOf(
            startBalance * onePercentRORProviders.mean * assetComposition[1].pct, .001)
    }

    should("determineNext uses current year if previous year not provided") {
        val assetCompositionEarly = listOf(
            AssetComposition("10 Pct Return", 1.0, tenPercentRORProvider),
        )
        val assetCompositionLater = listOf(
            AssetComposition("1 Pct Return", 1.0, onePercentRORProviders),
        )
        val assetCompMap = listOf(
            YearlyAssetComposition(currentDate.year, assetCompositionEarly),
            YearlyAssetComposition(2040, assetCompositionLater)
        )
        val progression = BasicAssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig.copy(assetCompMap = assetCompMap))

        val results = progression.determineNext(null)
        results.gains.size.shouldBe(1)
        results.gains[0].name.shouldBe(assetCompositionEarly[0].name)
        results.gains[0].amount.shouldBeWithinPercentageOf(
            startBalance * tenPercentRORProvider.mean, .001)
    }

    should("determineNext uses start amount if asset rec not found in previous year") {
        val assetCompositionEarly = listOf(
            AssetComposition("10 Pct Return", 1.0, tenPercentRORProvider),
        )
        val assetCompMap = listOf(
            YearlyAssetComposition(currentDate.year, assetCompositionEarly),
        )
        val progression = BasicAssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig.copy(assetCompMap = assetCompMap))

        val prevYearMissingAsset = prevYear.copy(assets = ArrayList())
        val results = progression.determineNext(prevYearMissingAsset)
        results.gains.size.shouldBe(1)
        results.gains[0].name.shouldBe(assetCompositionEarly[0].name)
        results.gains[0].amount.shouldBeWithinPercentageOf(
            startBalance * tenPercentRORProvider.mean, .001)
    }

    should("determineNext returns asset rec with taxable amounts") {
        val assetComposition = listOf(
            AssetComposition("10 Pct Return", 1.0, tenPercentRORProvider)
        )
        val assetCompMap = listOf(
            YearlyAssetComposition(2024, assetComposition)
        )
        val progression = BasicAssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig.copy(
                assetCompMap = assetCompMap,
                taxabilityProfile = NonWageTaxableProfile()),
        )

        val results = progression.determineNext(prevYear.copy(year = 2024))
        results.config.name.shouldBe(assetName)
        results.config.person.shouldBe(person)
        results.taxable.fed.shouldBe(results.gains[0].amount)
        results.taxable.state.shouldBe(results.gains[0].amount)
        results.taxable.socSec.shouldBe(0.0)
        results.taxable.medicare.shouldBe(0.0)
    }
})