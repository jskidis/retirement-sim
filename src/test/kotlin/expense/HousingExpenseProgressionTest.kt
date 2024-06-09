package expense

import RecIdentifier
import asset.AssetType
import asset.assetRecFixture
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class HousingExpenseProgressionTest : ShouldSpec({

    val year = currentDate.year + 1
    val ident = RecIdentifier(name = "House", person = "Household")
    val houseAsset = RecIdentifier("Family Home", person = "Household")
    val annualPAndI = 10000.0
    val annualTAndI = 5000.0
    val rentalEquiv = annualPAndI + annualTAndI
    val paidOffYear = year + 10
    val houseAssetRec = assetRecFixture(year, houseAsset, AssetType.ILLIQUID)

    val cmpdInflation = 2.0
    val inflationRec = inflationRecFixture(stdRAC = InflationRAC(
        rate = .03, cmpdStart = cmpdInflation - .03, cmpdEnd = cmpdInflation))

    val progression = HousingExpenseProgression(
        ident = ident,
        annualPAndI = annualPAndI,
        annualTAndI = annualTAndI,
        rentalEquiv = rentalEquiv,
        houseAsset = houseAsset,
        paidOffYear = paidOffYear
    )

    should("determineNext returns fixed P&I plus inflation adjusted T&I if house still owned and not payed off") {
        val prevYear = yearlyDetailFixture(year -1,
            inflation = inflationRec, assets = listOf(houseAssetRec))

        val result = progression.determineNext(prevYear)
        result.amount().shouldBe(annualPAndI + annualTAndI * cmpdInflation)
        result.taxDeductions.hasAmounts().shouldBeFalse()
    }

    should("determineNext returns fixed P&I plus T&I if prev year is null") {
        val result = progression.determineNext(null)
        result.amount().shouldBe(annualPAndI + annualTAndI)
        result.taxDeductions.hasAmounts().shouldBeFalse()
    }

    should("determineNext returns no P&I and inflation adjusted T&I if house still owned and payed off") {
        val prevYear = yearlyDetailFixture(paidOffYear,
            inflation = inflationRec, assets = listOf(houseAssetRec))

        val result = progression.determineNext(prevYear)
        result.amount().shouldBe(annualTAndI * cmpdInflation)
        result.taxDeductions.hasAmounts().shouldBeFalse()
    }

    should("determineNext returns rental equivalent is house no longer owned") {
        val prevYear = yearlyDetailFixture(year -1,
            inflation = inflationRec, assets = listOf())

        val result = progression.determineNext(prevYear)
        result.amount().shouldBe(rentalEquiv * cmpdInflation)
        result.taxDeductions.hasAmounts().shouldBeFalse()
    }
})
