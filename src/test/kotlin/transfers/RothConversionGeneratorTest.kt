package transfers

import Amount
import RecIdentifier
import asset.AssetChange
import asset.AssetRec
import asset.AssetType
import config.configFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import tax.NonWageTaxableProfile
import tax.TaxableAmounts
import util.SingleYearBasedConfig
import yearlyDetailFixture

class RothConversionGeneratorTest : ShouldSpec({

    val currYear = yearlyDetailFixture()
    val currentTaxable: Amount = 100000.0

    val baseConfig = configFixture().copy(
        taxesProcessor = tax.TaxesProcessorFixture(
            taxableAmounts = TaxableAmounts("Person", fed = currentTaxable)
        )
    )

    fun amountCalcConfig(currBracketTop: Amount) = SingleYearBasedConfig<RothConversionAmountCalc>(
        RothConversionAmountCalcFixture(currBracketTop - currentTaxable)
    )

    fun assetRec(ident: RecIdentifier, startBal: Amount) = AssetRec(
        year = currYear.year,
        ident = ident,
        assetType = AssetType.ROTH,
        startBal = startBal,
        startUnrealized = 0.0,
        gains = AssetChange("Person", 0.0)
    )

    val sourceIdent = RecIdentifier("Person", "SOURCE")
    val sourceIdent2 = RecIdentifier("Person", "SOURCE2")
    val destIdent = RecIdentifier("Person", "DEST")

    should("not process any conversion if amount to convert is 0") {
        val amountToConvert = 0.0
        val amountAvailable = 20000.0

        val sourceAsset = assetRec(sourceIdent, amountAvailable)
        val destAsset = assetRec(destIdent, 0.0)
        val yearProcessed = currYear.copy(assets = listOf(sourceAsset, destAsset))

        val amountCalcConfig = amountCalcConfig(currentTaxable + amountToConvert)
        val sourceDestPairs = listOf(Pair(sourceIdent, destIdent))

        val generator = RothConversionGenerator(
            amountCalc = amountCalcConfig,
            sourceDestPairs = sourceDestPairs,
            taxabilityProfile = NonWageTaxableProfile()
        )
        val config = baseConfig.copy(transferGenerators = listOf(generator))

        val infoResult = generator.determineTransferInfo(config, yearProcessed)
        infoResult.shouldBeNull()
    }

    should("not process any conversion if amount available is 0") {
        val amountToConvert = 20000.0
        val amountAvailable = 0.0

        val sourceAsset = assetRec(sourceIdent, amountAvailable)
        val destAsset = assetRec(destIdent, 0.0)
        val yearProcessed = currYear.copy(assets = listOf(sourceAsset, destAsset))

        val amountCalcConfig = amountCalcConfig(currentTaxable + amountToConvert)
        val sourceDestPairs = listOf(Pair(sourceIdent, destIdent))

        val generator = RothConversionGenerator(
            amountCalc = amountCalcConfig,
            sourceDestPairs = sourceDestPairs,
            taxabilityProfile = NonWageTaxableProfile()
        )
        val config = baseConfig.copy(transferGenerators = listOf(generator))

        val infoResult = generator.determineTransferInfo(config, yearProcessed)
        infoResult.shouldBeNull()
    }

    should("process a conversion of from source asset to dest asset if source asset balance > amount to convert") {
        val amountToConvert = 20000.0
        val amountAvailable = amountToConvert * 4.0

        val sourceAsset = assetRec(sourceIdent, amountAvailable)
        val destAsset = assetRec(destIdent, 0.0)
        val yearProcessed = currYear.copy(assets = listOf(sourceAsset, destAsset))

        val amountCalcConfig = amountCalcConfig(currentTaxable + amountToConvert)
        val sourceDestPairs = listOf(Pair(sourceIdent, destIdent))

        val generator = RothConversionGenerator(
            amountCalc = amountCalcConfig,
            sourceDestPairs = sourceDestPairs,
            taxabilityProfile = NonWageTaxableProfile()
        )
        val config = baseConfig.copy(transferGenerators = listOf(generator))

        val infoResult = generator.determineTransferInfo(config, yearProcessed)
        infoResult.shouldNotBeNull()
        infoResult.shouldBeTypeOf<RothConversationAmount>()
        infoResult.amount.shouldBe(amountToConvert)

        val transferResult = generator.performTransfers(yearProcessed, infoResult)
        transferResult.shouldHaveSize(1)
        transferResult[0].sourceTribution.name.shouldBe(RothConversionGenerator.ROTH_CONV_STR)
        transferResult[0].sourceTribution.amount.shouldBe(-amountToConvert)
        transferResult[0].sourceTribution.taxable.shouldBeNull()
        transferResult[0].sourceTribution.shouldBe(sourceAsset.tributions[0])

        transferResult[0].destTribution.name.shouldBe(RothConversionGenerator.ROTH_CONV_STR)
        transferResult[0].destTribution.amount.shouldBe(amountToConvert)
        transferResult[0].destTribution.taxable.shouldNotBeNull()
        transferResult[0].destTribution.taxable?.fed.shouldBe(amountToConvert)
        transferResult[0].destTribution.taxable?.fedLTG.shouldBe(0.0)
        transferResult[0].destTribution.taxable?.state.shouldBe(amountToConvert)
        transferResult[0].destTribution.isCarryOver.shouldBeTrue()
        transferResult[0].destTribution.shouldBe(destAsset.tributions[0])
    }

    should("process a conversion of from source asset to dest asset only up to asset balance of source asset") {
        val amountToConvert = 20000.0
        val amountAvailable = amountToConvert / 4.0

        val sourceAsset = assetRec(sourceIdent, amountAvailable)
        val destAsset = assetRec(destIdent, 0.0)
        val yearProcessed = currYear.copy(assets = listOf(sourceAsset, destAsset))

        val amountCalcConfig = amountCalcConfig(currentTaxable + amountToConvert)
        val sourceDestPairs = listOf(Pair(sourceIdent, destIdent))

        val generator = RothConversionGenerator(
            amountCalc = amountCalcConfig,
            sourceDestPairs = sourceDestPairs,
            taxabilityProfile = NonWageTaxableProfile()
        )
        val config = baseConfig.copy(transferGenerators = listOf(generator))

        val infoResult = generator.determineTransferInfo(config, yearProcessed)
        infoResult.shouldNotBeNull()
        infoResult.shouldBeTypeOf<RothConversationAmount>()
        infoResult.amount.shouldBe(amountAvailable)

        val transferResult = generator.performTransfers(yearProcessed, infoResult)
        transferResult.shouldHaveSize(1)

        transferResult[0].sourceTribution.name.shouldBe(RothConversionGenerator.ROTH_CONV_STR)
        transferResult[0].sourceTribution.amount.shouldBe(-amountAvailable)
        transferResult[0].sourceTribution.taxable.shouldBeNull()
        transferResult[0].sourceTribution.shouldBe(sourceAsset.tributions[0])

        transferResult[0].destTribution.name.shouldBe(RothConversionGenerator.ROTH_CONV_STR)
        transferResult[0].destTribution.amount.shouldBe(amountAvailable)
        transferResult[0].destTribution.isCarryOver.shouldBeTrue()
        transferResult[0].destTribution.shouldBe(destAsset.tributions[0])
    }

    should("process a conversion of from 1st source asset (up to balance) and then 2nd asset (then remainder) if 1st source asset doesn't have enough to cover") {
        val amountToConvert = 20000.0
        val amountAvailableFirst = amountToConvert / 4.0
        val amountAvailableSecond = amountToConvert - amountAvailableFirst

        val sourceAsset = assetRec(sourceIdent, amountAvailableFirst)
        val sourceAsset2 = assetRec(sourceIdent2, amountAvailableSecond)
        val destAsset = assetRec(destIdent, 0.0)
        val yearProcessed = currYear.copy(assets = listOf(sourceAsset, sourceAsset2, destAsset))

        val amountCalcConfig = amountCalcConfig(currentTaxable + amountToConvert)
        val sourceDestPairs = listOf(
            Pair(sourceIdent, destIdent),
            Pair(sourceIdent2, destIdent))

        val generator = RothConversionGenerator(
            amountCalc = amountCalcConfig,
            sourceDestPairs = sourceDestPairs,
            taxabilityProfile = NonWageTaxableProfile()
        )
        val config = baseConfig.copy(transferGenerators = listOf(generator))

        val infoResult = generator.determineTransferInfo(config, yearProcessed)
        infoResult.shouldNotBeNull()
        infoResult.shouldBeTypeOf<RothConversationAmount>()
        infoResult.amount.shouldBe(amountToConvert)

        val transferResult = generator.performTransfers(yearProcessed, infoResult)
        transferResult.shouldHaveSize(2)

        transferResult[0].sourceTribution.name.shouldBe(RothConversionGenerator.ROTH_CONV_STR)
        transferResult[0].sourceTribution.amount.shouldBe(-amountAvailableFirst)
        transferResult[0].sourceTribution.taxable.shouldBeNull()
        transferResult[0].sourceTribution.shouldBe(sourceAsset.tributions[0])

        transferResult[0].destTribution.name.shouldBe(RothConversionGenerator.ROTH_CONV_STR)
        transferResult[0].destTribution.amount.shouldBe(amountAvailableFirst)
        transferResult[0].destTribution.isCarryOver.shouldBeTrue()
        transferResult[0].destTribution.shouldBe(destAsset.tributions[0])

        transferResult[1].sourceTribution.name.shouldBe(RothConversionGenerator.ROTH_CONV_STR)
        transferResult[1].sourceTribution.amount.shouldBe(-amountAvailableSecond)
        transferResult[1].sourceTribution.taxable.shouldBeNull()
        transferResult[1].sourceTribution.shouldBe(sourceAsset2.tributions[0])

        transferResult[1].destTribution.name.shouldBe(RothConversionGenerator.ROTH_CONV_STR)
        transferResult[1].destTribution.amount.shouldBe(amountAvailableSecond)
        transferResult[1].destTribution.isCarryOver.shouldBeTrue()
        transferResult[1].destTribution.shouldBe(destAsset.tributions[1])
    }

    should("process a conversion of from 2ns source asset to dest asset if first source is closed") {
        val amountToConvert = 20000.0
        val amountAvailable = amountToConvert * 4.0

        val sourceAsset2 = assetRec(sourceIdent2, amountAvailable)
        val destAsset = assetRec(destIdent, 0.0)
        val yearProcessed = currYear.copy(assets = listOf(sourceAsset2, destAsset))

        val amountCalcConfig = amountCalcConfig(currentTaxable + amountToConvert)
        val sourceDestPairs = listOf(
            Pair(sourceIdent, destIdent),
            Pair(sourceIdent2, destIdent)
        )

        val generator = RothConversionGenerator(
            amountCalc = amountCalcConfig,
            sourceDestPairs = sourceDestPairs,
            taxabilityProfile = NonWageTaxableProfile()
        )
        val config = baseConfig.copy(transferGenerators = listOf(generator))

        val infoResult = generator.determineTransferInfo(config, yearProcessed)
        infoResult.shouldNotBeNull()
        infoResult.shouldBeTypeOf<RothConversationAmount>()
        infoResult.amount.shouldBe(amountToConvert)

        val transferResult = generator.performTransfers(yearProcessed, infoResult)
        transferResult.shouldHaveSize(1)

        transferResult[0].sourceTribution.shouldBe(sourceAsset2.tributions[0])
        transferResult[0].destTribution.shouldBe(destAsset.tributions[0])

        sourceAsset2.tributions.shouldHaveSize(1)
        sourceAsset2.tributions[0].name.shouldBe(RothConversionGenerator.ROTH_CONV_STR)
        sourceAsset2.tributions[0].amount.shouldBe(-amountToConvert)
        sourceAsset2.tributions[0].taxable.shouldBeNull()
    }
})
