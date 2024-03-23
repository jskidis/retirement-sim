package asset

import Amount
import Name
import config.personFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import util.YearBasedConfig
import util.YearConfigPair
import util.currentDate
import yearlyDetailFixture

class AssetProgressionTest : ShouldSpec({

    val assetName: Name = "Asset Name"
    val person: Name = "Person"
    val startBalance: Amount = 1000.0
    val startUnrealized: Amount = 500.0

    val tenPctReturn = PortfolAttribs(name="Ten Pct", mean = 0.10, stdDev = 0.01)
    val onePctReturn = PortfolAttribs(name="One Pct", mean = 0.01, stdDev = 0.001)

    val baseAssetConfig = assetConfigFixture(assetName, person)
    val prevAssetRec = assetRecFixture(
        assetConfig = baseAssetConfig, startBal = startBalance, startUnrealized = startUnrealized
    )

    val prevYear = yearlyDetailFixture().copy(assets = listOf(prevAssetRec))

    should("determineNext returns asset rec same gain all years)") {
        val attributeSet = YearBasedConfig(listOf(
            YearConfigPair(2024, tenPctReturn)
        ))
        val progression = AssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig,
            gainCreator = SimpleAssetGainCreator(),
            attributesSet = attributeSet
        )

        val results = progression.determineNext(prevYear.copy(year = 2024))
        results.config.name.shouldBe(assetName)
        results.config.person.shouldBe(person)
        results.gains.name.shouldBe(tenPctReturn.name)
        results.gains.amount.shouldBeWithinPercentageOf(
            startBalance * tenPctReturn.mean, .001)
        results.totalUnrealized().shouldBe(startUnrealized)
        results.tributions.shouldBeEmpty()
    }

    should("determineNext returns asset rec for different years)") {
        val attributeSet = YearBasedConfig(listOf(
            YearConfigPair(currentDate.year, tenPctReturn),
            YearConfigPair(2030, onePctReturn)
        ))
        val progression = AssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig,
            gainCreator = SimpleAssetGainCreator(),
            attributesSet = attributeSet
        )

        val results2024 = progression.determineNext(prevYear.copy(year = 2024))
        results2024.gains.name.shouldBe(tenPctReturn.name)
        results2024.gains.amount.shouldBeWithinPercentageOf(
            startBalance * tenPctReturn.mean, .001)

        val results2034 = progression.determineNext(prevYear.copy(
            year = 2034, assets = listOf(prevAssetRec.copy(year = 2034)))
        )
        results2034.gains.name.shouldBe(onePctReturn.name)
        results2034.gains.amount.shouldBeWithinPercentageOf(
            startBalance * onePctReturn.mean, .001)
    }

    should("determineNext uses current year if previous year not provided") {
        val attributeSet = YearBasedConfig(listOf(
            YearConfigPair(currentDate.year, tenPctReturn),
            YearConfigPair(2040, onePctReturn)
        ))
        val progression = AssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig,
            gainCreator = SimpleAssetGainCreator(),
            attributesSet = attributeSet
        )

        val results = progression.determineNext(null)
        results.gains.name.shouldBe(tenPctReturn.name)
        results.gains.amount.shouldBeWithinPercentageOf(
            startBalance * tenPctReturn.mean, .001)
        results.startUnrealized.shouldBe(0.0)
        results.tributions.shouldBeEmpty()
    }

    should("determineNext assumes 0 if asset rec not found in previous year") {
        val attributeSet = YearBasedConfig(listOf(
            YearConfigPair(2024, tenPctReturn)
        ))
        val progression = AssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig,
            gainCreator = SimpleAssetGainCreator(),
            attributesSet = attributeSet
        )

        val prevYearMissingAsset = prevYear.copy(assets = listOf())
        val results = progression.determineNext(prevYearMissingAsset)
        results.gains.name.shouldBe(tenPctReturn.name)
        results.gains.amount.shouldBe(0.0)
        results.startUnrealized.shouldBe(0.0)
        results.tributions.shouldBeEmpty()
    }

    should("determineNext creates req dist is req dist handler returns)") {
        val rmdPct = 0.10

        val attributeSet = YearBasedConfig(
            listOf(
                YearConfigPair(2024, tenPctReturn)
            ))
        val progression = AssetProgression(
            startBalance = startBalance,
            config = baseAssetConfig,
            gainCreator = SimpleAssetGainCreator(),
            attributesSet = attributeSet,
            requiredDistHandler = RmdRequiredDistFixture(personFixture(), rmdPct)
        )

        val results = progression.determineNext(prevYear.copy(year = 2024))
        results.tributions.shouldNotBeEmpty()
        results.tributions[0].amount.shouldBe(-startBalance * rmdPct)
        results.tributions[0].name.shouldBe(RequiredDistHandler.CHANGE_NAME)
        results.tributions[0].isCarryOver.shouldBeFalse()
        results.tributions[0].isReqDist.shouldBeTrue()
    }
})

