package transfers

import RecIdentifier
import asset.AssetChange
import asset.assetRecFixture
import config.configFixture
import departed.DepartedRec
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import tax.NonWageTaxableProfile
import util.currentDate
import yearlyDetailFixture

class CloseAccountsOnDepartureTest : ShouldSpec({
    val year = currentDate.year +1
    val transferName = "TOD-Trasnfer"
    val taxabilityProfile = NonWageTaxableProfile()
    val personName = "Person"

    val emptySourceAssetIdent = RecIdentifier("Empty Account", personName)
    val emptySourceAssetRec = assetRecFixture(year, emptySourceAssetIdent, startBal = 0.0)

    val sourceAsset1StartBal = 1000.0
    val sourceAsset1GainAmt = 100.0
    val sourceAsset1Ident = RecIdentifier("Source1", personName)
    val sourceAssetRec1 = assetRecFixture(year, sourceAsset1Ident,
        startBal = sourceAsset1StartBal, gains = AssetChange("Gain", sourceAsset1GainAmt))

    val sourceAsset2StartBal = 2000.0
    val sourceAsset2GainAmt = 200.0
    val sourceAsset2Ident = RecIdentifier("Source2", personName)
    val sourceAssetRec2 = assetRecFixture(year, sourceAsset2Ident,
        startBal = sourceAsset2StartBal, gains = AssetChange("Gain", sourceAsset2GainAmt))

    val destAsset1Ident = RecIdentifier("Dest1", personName)
    val destAsset1Rec = assetRecFixture(year, destAsset1Ident)

    val destAsset2Ident = RecIdentifier("Dest2", personName)
    val destAsset2Rec = assetRecFixture(year, destAsset2Ident)

    val simConfig = configFixture()

    should("determineTransferAmount returns 0 if year is not year person departed") {
        val sourceDestPairs = listOf(sourceAsset1Ident to destAsset1Ident)
        val assets = listOf(sourceAssetRec1, destAsset1Rec)

        val currYear = yearlyDetailFixture(year, assets = assets, departed = listOf())
        val transferGenerator = CloseAccountsOnDeparture(
            personName, transferName, sourceDestPairs, taxabilityProfile)

        transferGenerator.determineTransferAmount(simConfig, currYear).shouldBeZero()
    }

    should("determineTransferAmount returns 0 if year is correct but no balances in source assets to transfer") {
        val sourceDestPairs = listOf(emptySourceAssetIdent to destAsset1Ident)
        val assets = listOf(emptySourceAssetRec, destAsset1Rec)

        val currYear = yearlyDetailFixture(year, assets = assets,
            departed = listOf(DepartedRec(personName, year)))

        val transferGenerator = CloseAccountsOnDeparture(
            personName, transferName, sourceDestPairs, taxabilityProfile)

        transferGenerator.determineTransferAmount(simConfig, currYear).shouldBeZero()
    }

    should("determineTransferAmount returns sum of all (final) balances in source recs") {
        val sourceDestPairs = listOf(
            sourceAsset1Ident to destAsset1Ident,
            sourceAsset2Ident to destAsset2Ident
        )
        val assets = listOf(sourceAssetRec1, sourceAssetRec2, destAsset1Rec, destAsset2Rec)

        val currYear = yearlyDetailFixture(year, assets = assets,
            departed = listOf(DepartedRec(personName, year)))

        val transferGenerator = CloseAccountsOnDeparture(
            personName, transferName, sourceDestPairs, taxabilityProfile)

        val expectedAmount = sourceAsset1StartBal + sourceAsset1GainAmt +
            sourceAsset2StartBal + sourceAsset2GainAmt
        transferGenerator.determineTransferAmount(simConfig, currYear).shouldBe(expectedAmount)
    }

    should("generateTransfer should generate a transfer for each source-dest pair") {
        val sourceDestPairs = listOf(
            sourceAsset1Ident to destAsset1Ident,
            sourceAsset2Ident to destAsset2Ident
        )
        val assets = listOf(sourceAssetRec1, sourceAssetRec2, destAsset1Rec, destAsset2Rec)

        val currYear = yearlyDetailFixture(year, assets = assets,
            departed = listOf(DepartedRec(personName, year)))

        val transferGenerator = CloseAccountsOnDeparture(
            personName, transferName, sourceDestPairs, taxabilityProfile)

        val result = transferGenerator.generateTransfers(currYear)
        result.shouldHaveSize(2)
        result[0].sourceRec.shouldBe(sourceAssetRec1)
        result[0].destRec.shouldBe(destAsset1Rec)
        result[0].sourceTribution.name.shouldBe(transferName)
        result[0].sourceTribution.cashflow.shouldBeZero()
        result[0].sourceTribution.amount.shouldBe(-sourceAsset1StartBal - sourceAsset1GainAmt)
        result[0].destTribution.name.shouldBe(transferName)
        result[0].destTribution.cashflow.shouldBeZero()
        result[0].destTribution.amount.shouldBe(sourceAsset1StartBal + sourceAsset1GainAmt)

        result[1].sourceRec.shouldBe(sourceAssetRec2)
        result[1].destRec.shouldBe(destAsset2Rec)
        result[1].sourceTribution.name.shouldBe(transferName)
        result[1].sourceTribution.cashflow.shouldBeZero()
        result[1].sourceTribution.amount.shouldBe(-sourceAsset2StartBal - sourceAsset2GainAmt)
        result[1].destTribution.name.shouldBe(transferName)
        result[1].destTribution.cashflow.shouldBeZero()
        result[1].destTribution.amount.shouldBe(sourceAsset2StartBal + sourceAsset2GainAmt)
    }
})
