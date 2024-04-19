package transfers

import Amount
import RecIdentifier
import YearlyDetail
import asset.AssetChange
import asset.assetRecFixture
import config.SimConfig
import config.configFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import tax.NonWageTaxableProfile
import tax.TaxabilityProfile
import util.currentDate
import yearlyDetailFixture

class TransferProcessorTest : ShouldSpec({

    val amount1 = 100.0
    val sourceAssetRec1Ident = RecIdentifier("Source Asset 1", "Person")
    val sourceAssetRec1 = assetRecFixture(ident = sourceAssetRec1Ident)
    val sourceTribution1 = AssetChange("Asset1 Distribution", -amount1)

    val destAssetRec1Ident = RecIdentifier("Dest Asset 1", "Person")
    val destAssetRec1 = assetRecFixture(ident = destAssetRec1Ident)
    val destTribution1 = AssetChange("Asset1 Distribution", amount1)

    val amount2 = 0.0
    val sourceAssetRec2Ident = RecIdentifier("Source Asset 2", "Person")
    val sourceAssetRec2 = assetRecFixture(ident = sourceAssetRec2Ident)

    val destAssetRec2Ident = RecIdentifier("Dest Asset 2", "Person")
    val destAssetRec2 = assetRecFixture(ident = destAssetRec2Ident)

    val amount3_1 = 310.0
    val amount3_2 = 320.0

    val sourceAssetRec3_1Ident = RecIdentifier("Source Asset 3.1", "Person")
    val sourceAssetRec3_1 = assetRecFixture(ident = sourceAssetRec3_1Ident)
    val sourceAssetRec3_2Ident = RecIdentifier("Source Asset 3.2", "Person")
    val sourceAssetRec3_2 = assetRecFixture(ident = sourceAssetRec3_2Ident)
    val sourceTribution3_1 = AssetChange("Asset3.1 Distribution", -amount3_1)
    val sourceTribution3_2 = AssetChange("Asset3.2 Distribution", -amount3_2)


    val destAssetRec3Ident = RecIdentifier("Dest Asset 3", "Person")
    val destAssetRec3 = assetRecFixture(ident = destAssetRec3Ident)
    val destTribution3_1 = AssetChange("Asset3.1 Distribution", amount3_1)
    val destTribution3_2 = AssetChange("Asset3.2 Distribution", amount3_2)

    should("processes transfers") {
        val transferRec1 = TransferRec(
            sourceRec = sourceAssetRec1, sourceTribution = sourceTribution1,
            destRec = destAssetRec1, destTribution = destTribution1
        )
        // Note: This should never be used since the amount to transfer is 0
        val transferRec2 = TransferRec(
            sourceRec = sourceAssetRec2, sourceTribution = sourceTribution1,
            destRec = destAssetRec2, destTribution = destTribution1
        )
        val transferRec3_1 = TransferRec(
            sourceRec = sourceAssetRec3_1, sourceTribution = sourceTribution3_1,
            destRec = destAssetRec3, destTribution = destTribution3_1
        )
        val transferRec3_2 = TransferRec(
            sourceRec = sourceAssetRec3_2, sourceTribution = sourceTribution3_2,
            destRec = destAssetRec3, destTribution = destTribution3_2
        )

        val generator1 = TransferGeneratorFixture(
            transferAmount = amount1, transferRecs = listOf(transferRec1))

        val generator2 = TransferGeneratorFixture(
            transferAmount = amount2, transferRecs = listOf(transferRec2)
        )

        val generator3 = TransferGeneratorFixture(
            transferAmount = amount3_1 + amount3_2,
            transferRecs = listOf(transferRec3_1, transferRec3_2)
        )

        val year = currentDate.year +1
        val config = configFixture(year,
            transferGenerators = listOf(generator1, generator2, generator3))
        val currYear = yearlyDetailFixture(year)

        val result = TransferProcessor.process(config, currYear)
        result.shouldHaveSize(3)
        result[0].shouldBe(transferRec1)
        result[1].shouldBe(transferRec3_1)
        result[2].shouldBe(transferRec3_2)

        sourceAssetRec1.tributions.shouldHaveSize(1)
        sourceAssetRec1.tributions[0].shouldBe(sourceTribution1)
        destAssetRec1.tributions.shouldHaveSize(1)
        destAssetRec1.tributions[0].shouldBe(destTribution1)

        sourceAssetRec2.tributions.shouldBeEmpty()
        destAssetRec2.tributions.shouldBeEmpty()

        sourceAssetRec3_1.tributions.shouldHaveSize(1)
        sourceAssetRec3_1.tributions[0].shouldBe(sourceTribution3_1)
        sourceAssetRec3_2.tributions.shouldHaveSize(1)
        sourceAssetRec3_2.tributions[0].shouldBe(sourceTribution3_2)

        destAssetRec3.tributions.shouldHaveSize(2)
        destAssetRec3.tributions[0].shouldBe(destTribution3_1)
        destAssetRec3.tributions[1].shouldBe(destTribution3_2)
    }
})

class TransferGeneratorFixture(
    val transferAmount: Amount,
    val transferRecs: List<TransferRec>,
) : TransferGenerator {

    override val taxabilityProfile: TaxabilityProfile = NonWageTaxableProfile()
    override val sourceDestPairs: List<Pair<RecIdentifier, RecIdentifier>> =
        transferRecs.map { it.sourceRec.ident to it.destRec.ident }

    override fun determineTransferInfo(config: SimConfig, currYear: YearlyDetail)
        : Amount = transferAmount

    override fun performTransfers(currYear: YearlyDetail, amount: Amount)
        : List<TransferRec> = transferRecs
}
