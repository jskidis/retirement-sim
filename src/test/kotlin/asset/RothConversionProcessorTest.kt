package asset

import Amount
import RecIdentifier
import config.RothConversionConfig
import config.configFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import tax.NonWageTaxableProfile
import tax.TaxableAmounts
import util.YearBasedConfig
import util.YearConfigPair
import yearlyDetailFixture

class RothConversionProcessorTest : ShouldSpec({

    val currYear = yearlyDetailFixture()
    val currentTaxable: Amount = 100000.0

    val baseConfig = configFixture().copy(
        taxesProcessor = tax.TaxesProcessorFixture(
            taxableAmounts = TaxableAmounts("Person", fed = currentTaxable)
        )
    )

    fun amountCalcConfig(currBracketTop: Amount): YearBasedConfig<RothConversionAmountCalc> {
        return YearBasedConfig(
            listOf(
                YearConfigPair(
                    startYear = 2000, config =
                    RothConversionAmountCalcFixture(currBracketTop - currentTaxable)
                )
            ))
    }

    fun assetRec(ident: RecIdentifier, startBal: Amount) = AssetRec(
        year = currYear.year,
        ident = ident,
        startBal = startBal,
        startUnrealized = 0.0,
        gains = AssetChange("Person", 0.0)
    )

    val sourceIdent = RecIdentifier("Person", "SOURCE")
    val sourceIdent2 = RecIdentifier("Person", "SOURCE2")
    val destIdent = RecIdentifier("Person", "DEST")

    should("process nothing if no roth conversion config") {
        RothConversionProcessor.process(baseConfig, currYear).shouldBe(0.0)
    }

    should("process a conversion of from source asset to dest asset if source asset balance > amount to convert") {
        val amountToConvert = 20000.0
        val amountAvailable = amountToConvert * 4.0

        val sourceAsset = assetRec(sourceIdent, amountAvailable)
        val destAsset = assetRec(destIdent, 0.0)
        val yearProcessed = currYear.copy(assets = listOf(sourceAsset, destAsset))

        val amountCalcConfig = amountCalcConfig(currentTaxable + amountToConvert)
        val sourceDestPairs = listOf(Pair(sourceIdent, destIdent))

        val config = baseConfig.copy(rothConversion = RothConversionConfig(
            amountCalc = amountCalcConfig,
            sourceDestPairs = sourceDestPairs,
            taxabilityProfile = NonWageTaxableProfile()
        ))

        val result = RothConversionProcessor.process(config, yearProcessed)
        result.shouldBe(amountToConvert)

        sourceAsset.tributions.shouldHaveSize(1)
        sourceAsset.tributions[0].name.shouldBe(RothConversionProcessor.ROTH_CONV_STR)
        sourceAsset.tributions[0].amount.shouldBe(-amountToConvert)
        sourceAsset.tributions[0].taxable.shouldBeNull()

        destAsset.tributions.shouldHaveSize(1)
        destAsset.tributions[0].name.shouldBe(RothConversionProcessor.ROTH_CONV_STR)
        destAsset.tributions[0].amount.shouldBe(amountToConvert)
        destAsset.tributions[0].taxable.shouldNotBeNull()
        destAsset.tributions[0].taxable?.fed.shouldBe(amountToConvert)
        destAsset.tributions[0].taxable?.fedLTG.shouldBe(0.0)
        destAsset.tributions[0].taxable?.state.shouldBe(amountToConvert)
        destAsset.tributions[0].isCarryOver.shouldBeTrue()
    }

    should("process a conversion of from source asset to dest asset only up to asset balance of source asset") {
        val amountToConvert = 20000.0
        val amountAvailable = amountToConvert / 4.0

        val sourceAsset = assetRec(sourceIdent, amountAvailable)
        val destAsset = assetRec(destIdent, 0.0)
        val yearProcessed = currYear.copy(assets = listOf(sourceAsset, destAsset))

        val amountCalcConfig = amountCalcConfig(currentTaxable + amountToConvert)
        val sourceDestPairs = listOf(Pair(sourceIdent, destIdent))

        val config = baseConfig.copy(rothConversion = RothConversionConfig(
            amountCalc = amountCalcConfig,
            sourceDestPairs = sourceDestPairs,
            taxabilityProfile = NonWageTaxableProfile()
        ))

        val result = RothConversionProcessor.process(config, yearProcessed)
        result.shouldBe(amountAvailable)

        sourceAsset.tributions.shouldHaveSize(1)
        sourceAsset.tributions[0].name.shouldBe(RothConversionProcessor.ROTH_CONV_STR)
        sourceAsset.tributions[0].amount.shouldBe(-amountAvailable)
        sourceAsset.tributions[0].taxable.shouldBeNull()

        destAsset.tributions.shouldHaveSize(1)
        destAsset.tributions[0].name.shouldBe(RothConversionProcessor.ROTH_CONV_STR)
        destAsset.tributions[0].amount.shouldBe(amountAvailable)
        destAsset.tributions[0].taxable.shouldNotBeNull()
        destAsset.tributions[0].isCarryOver.shouldBeTrue()
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

        val config = baseConfig.copy(rothConversion = RothConversionConfig(
            amountCalc = amountCalcConfig,
            sourceDestPairs = sourceDestPairs,
            taxabilityProfile = NonWageTaxableProfile()
        ))

        val result = RothConversionProcessor.process(config, yearProcessed)
        result.shouldBe(amountToConvert)

        sourceAsset.tributions.shouldHaveSize(1)
        sourceAsset.tributions[0].name.shouldBe(RothConversionProcessor.ROTH_CONV_STR)
        sourceAsset.tributions[0].amount.shouldBe(-amountAvailableFirst)
        sourceAsset.tributions[0].taxable.shouldBeNull()

        sourceAsset2.tributions.shouldHaveSize(1)
        sourceAsset2.tributions[0].name.shouldBe(RothConversionProcessor.ROTH_CONV_STR)
        sourceAsset2.tributions[0].amount.shouldBe(-amountAvailableSecond)
        sourceAsset2.tributions[0].taxable.shouldBeNull()

        destAsset.tributions.shouldHaveSize(2)
        destAsset.tributions[0].name.shouldBe(RothConversionProcessor.ROTH_CONV_STR)
        destAsset.tributions[0].amount.shouldBe(amountAvailableFirst)
        destAsset.tributions[0].taxable.shouldNotBeNull()
        destAsset.tributions[0].isCarryOver.shouldBeTrue()

        destAsset.tributions[1].name.shouldBe(RothConversionProcessor.ROTH_CONV_STR)
        destAsset.tributions[1].amount.shouldBe(amountAvailableSecond)
        destAsset.tributions[1].taxable.shouldNotBeNull()
        destAsset.tributions[1].isCarryOver.shouldBeTrue()
    }

    should("process a conversion of from 1st source asset to dest asset only up to asset balance of 1st source asset") {
        val amountToConvert = 20000.0
        val amountAvailable = amountToConvert * 4.0

        val sourceAsset = assetRec(sourceIdent, amountAvailable)
        val sourceAsset2 = assetRec(sourceIdent2, Amount.MAX_VALUE)
        val destAsset = assetRec(destIdent, 0.0)
        val yearProcessed = currYear.copy(assets = listOf(sourceAsset, sourceAsset2, destAsset))

        val amountCalcConfig = amountCalcConfig(currentTaxable + amountToConvert)
        val sourceDestPairs = listOf(
            Pair(sourceIdent, destIdent),
            Pair(sourceIdent2, destIdent))

        val config = baseConfig.copy(rothConversion = RothConversionConfig(
            amountCalc = amountCalcConfig,
            sourceDestPairs = sourceDestPairs,
            taxabilityProfile = NonWageTaxableProfile()
        ))

        val result = RothConversionProcessor.process(config, yearProcessed)
        result.shouldBe(amountToConvert)

        sourceAsset.tributions.shouldHaveSize(1)
        sourceAsset.tributions[0].name.shouldBe(RothConversionProcessor.ROTH_CONV_STR)
        sourceAsset.tributions[0].amount.shouldBe(-amountToConvert)
        sourceAsset.tributions[0].taxable.shouldBeNull()

        sourceAsset2.tributions.shouldBeEmpty()

        destAsset.tributions.shouldHaveSize(1)
        destAsset.tributions[0].name.shouldBe(RothConversionProcessor.ROTH_CONV_STR)
        destAsset.tributions[0].amount.shouldBe(amountToConvert)
        destAsset.tributions[0].taxable.shouldNotBeNull()
        destAsset.tributions[0].isCarryOver.shouldBeTrue()
    }
})
