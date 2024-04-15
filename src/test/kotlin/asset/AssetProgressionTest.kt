package asset

import Amount
import Name
import RecIdentifier
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import tax.NonTaxableProfile
import util.YearBasedConfig
import util.YearConfigPair
import util.currentDate
import yearlyDetailFixture

class AssetProgressionTest : ShouldSpec({

    val nextYear = currentDate.year + 1

    val assetName: Name = "Asset Name"
    val person: Name = "Person"
    val startBalance: Amount = 1000.0
    val startUnrealized: Amount = 500.0

    val tenPctReturn = PortfolioAttribs(name = "Ten Pct", mean = 0.10, stdDev = 0.01)
    val onePctReturn = PortfolioAttribs(name = "One Pct", mean = 0.01, stdDev = 0.001)

    val baseAssetIdent = RecIdentifier(assetName, person)
    val prevAssetRec = assetRecFixture(
        ident = baseAssetIdent, startBal = startBalance, startUnrealized = startUnrealized
    )

    val prevYear = yearlyDetailFixture().copy(assets = listOf(prevAssetRec))

    should("determineNext returns asset rec same gain all years)") {
        val attributeSet = YearBasedConfig(
            listOf(
                YearConfigPair(2024, tenPctReturn)
            ))
        val progression = AssetProgression(
            ident = baseAssetIdent,
            startBalance = startBalance,
            assetType = AssetType.CASH,
            gainCreator = SimpleAssetGainCreator(
                taxability = NonTaxableProfile(),
                attributesSet = attributeSet
            )
        )

        val results = progression.determineNext(prevYear.copy(year = nextYear))
        results.ident.name.shouldBe(assetName)
        results.ident.person.shouldBe(person)
        results.gains.name.shouldBe(tenPctReturn.name)
        results.gains.amount.shouldBeWithinPercentageOf(
            startBalance * tenPctReturn.mean, .001)
        results.totalUnrealized().shouldBe(startUnrealized)
        results.tributions.shouldBeEmpty()
    }

    should("determineNext returns asset rec for different years)") {
        val attributeSet = YearBasedConfig(
            listOf(
                YearConfigPair(currentDate.year, tenPctReturn),
                YearConfigPair(nextYear + 5, onePctReturn)
            ))
        val progression = AssetProgression(
            startBalance = startBalance,
            ident = baseAssetIdent,
            assetType = AssetType.CASH,
            gainCreator = SimpleAssetGainCreator(
                taxability = NonTaxableProfile(),
                attributesSet = attributeSet
            )
        )

        val resultsNextYear = progression.determineNext(prevYear.copy(year = nextYear))
        resultsNextYear.gains.name.shouldBe(tenPctReturn.name)
        resultsNextYear.gains.amount.shouldBeWithinPercentageOf(
            startBalance * tenPctReturn.mean, .001)

        val results10YearsAhead = progression.determineNext(
            prevYear.copy(
                year = nextYear + 10, assets = listOf(prevAssetRec.copy(year = nextYear + 10)))
        )
        results10YearsAhead.gains.name.shouldBe(onePctReturn.name)
        results10YearsAhead.gains.amount.shouldBeWithinPercentageOf(
            startBalance * onePctReturn.mean, .001)
    }

    should("determineNext uses current year if previous year not provided") {
        val attributeSet = YearBasedConfig(
            listOf(
                YearConfigPair(currentDate.year, tenPctReturn),
                YearConfigPair(nextYear + 100, onePctReturn)
            ))
        val progression = AssetProgression(
            startBalance = startBalance,
            ident = baseAssetIdent,
            assetType = AssetType.CASH,
            gainCreator = SimpleAssetGainCreator(
                taxability = NonTaxableProfile(),
                attributesSet = attributeSet
            )
        )

        val results = progression.determineNext(null)
        results.gains.name.shouldBe(tenPctReturn.name)
        results.gains.amount.shouldBeWithinPercentageOf(
            startBalance * tenPctReturn.mean, .001)
        results.startUnrealized.shouldBe(0.0)
        results.tributions.shouldBeEmpty()
    }

    should("determineNext assumes 0 if asset rec not found in previous year") {
        val attributeSet = YearBasedConfig(
            listOf(
                YearConfigPair(nextYear, tenPctReturn)
            ))
        val progression = AssetProgression(
            startBalance = startBalance,
            assetType = AssetType.CASH,
            ident = baseAssetIdent,
            gainCreator = SimpleAssetGainCreator(
                taxability = NonTaxableProfile(),
                attributesSet = attributeSet
            )
        )

        val prevYearMissingAsset = prevYear.copy(assets = listOf())
        val results = progression.determineNext(prevYearMissingAsset)
        results.gains.name.shouldBe(tenPctReturn.name)
        results.gains.amount.shouldBe(0.0)
        results.startUnrealized.shouldBe(0.0)
        results.tributions.shouldBeEmpty()
    }
})

