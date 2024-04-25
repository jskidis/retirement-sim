package transfers

import Amount
import RecIdentifier
import asset.AssetChange
import asset.AssetRec
import asset.AssetType
import config.configFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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

    fun validateTribution(tribution: AssetChange, amount: Amount) {
        tribution.name.shouldBe(RothConversionGenerator.ROTH_CONV_STR)
        tribution.amount.shouldBe(amount)
        tribution.cashflow.shouldBeZero()
    }

    fun validateSourceTransferRec(transferRec: TransferRec, sourceRec: AssetRec, amount: Amount) {
        transferRec.sourceRec.shouldBe(sourceRec)
        validateTribution(transferRec.sourceTribution, amount)
        transferRec.sourceTribution.taxable.shouldBeNull()
        transferRec.sourceTribution.isCarryOver.shouldBeFalse()
    }

    fun validateDestTransferRec(transferRec: TransferRec, destRec: AssetRec, amount: Amount) {
        transferRec.destRec.shouldBe(destRec)
        validateTribution(transferRec.destTribution, amount)
        transferRec.destTribution.taxable.shouldNotBeNull()
        transferRec.destTribution.taxable?.fed.shouldBe(amount)
        transferRec.destTribution.taxable?.fedLTG.shouldBe(0.0)
        transferRec.destTribution.taxable?.state.shouldBe(amount)
        transferRec.destTribution.isCarryOver.shouldBeTrue()
    }

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

        val infoResult = generator.determineTransferAmount(config, yearProcessed)
        infoResult.shouldBeZero()
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

        val infoResult = generator.determineTransferAmount(config, yearProcessed)
        infoResult.shouldBeZero()
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

        val infoResult = generator.determineTransferAmount(config, yearProcessed)
        infoResult.shouldBe(amountToConvert)

        val transferResult = generator.generateTransfers(yearProcessed, infoResult)
        transferResult.shouldHaveSize(1)
        validateSourceTransferRec(transferResult[0], sourceAsset, -amountToConvert)
        validateDestTransferRec(transferResult[0], destAsset, amountToConvert)
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

        val infoResult = generator.determineTransferAmount(config, yearProcessed)
        infoResult.shouldBe(amountAvailable)

        val transferResult = generator.generateTransfers(yearProcessed, infoResult)
        transferResult.shouldHaveSize(1)
        validateSourceTransferRec(transferResult[0], sourceAsset, -amountAvailable)
        validateDestTransferRec(transferResult[0], destAsset, amountAvailable)
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

        val infoResult = generator.determineTransferAmount(config, yearProcessed)
        infoResult.shouldBe(amountToConvert)

        val transferResult = generator.generateTransfers(yearProcessed, infoResult)
        transferResult.shouldHaveSize(2)
        validateSourceTransferRec(transferResult[0], sourceAsset, -amountAvailableFirst)
        validateDestTransferRec(transferResult[0], destAsset, amountAvailableFirst)
        validateSourceTransferRec(transferResult[1], sourceAsset2, -amountAvailableSecond)
        validateDestTransferRec(transferResult[1], destAsset, amountAvailableSecond)
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

        val infoResult = generator.determineTransferAmount(config, yearProcessed)
        infoResult.shouldBe(amountToConvert)

        val transferResult = generator.generateTransfers(yearProcessed, infoResult)
        transferResult.shouldHaveSize(1)
        validateSourceTransferRec(transferResult[0], sourceAsset2, -amountToConvert)
        validateDestTransferRec(transferResult[0], destAsset, amountToConvert)
    }
})
