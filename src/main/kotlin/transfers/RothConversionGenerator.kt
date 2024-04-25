package transfers

import Amount
import RecIdentifier
import YearlyDetail
import config.SimConfig
import tax.TaxabilityProfile
import util.RecFinder
import util.YearBasedConfig

open class RothConversionGenerator(
    val amountCalc: YearBasedConfig<RothConversionAmountCalc>,
    override val sourceDestPairs: List<Pair<RecIdentifier, RecIdentifier>>,
    override val taxabilityProfile: TaxabilityProfile,
) : TransferGenerator,
    TransferRecGenerator by SimpleTransferRecGenerator(ROTH_CONV_STR, taxabilityProfile) {

    companion object {
        const val ROTH_CONV_STR = "RothConv"
    }

    override fun transferName(): String = ROTH_CONV_STR

    override fun determineTransferAmount(config: SimConfig, currYear: YearlyDetail): Amount {
        val taxableAmounts = config.taxesProcessor.determineTaxableAmounts(currYear)
        val amountCalc = amountCalc.getConfigForYear(currYear.year)
        val amount = amountCalc.amountToConvert(
            currYear, taxableAmounts, config.currTaxConfig(currYear))

        return Math.min(amount, availableToTransfer(currYear))
    }

    override fun generateTransfers(currYear: YearlyDetail, amount: Amount): List<TransferRec> {
        val transferRecs: MutableList<TransferRec> = mutableListOf()

        sourceDestPairs.fold(amount) { acc, it ->
            if (acc < 1.0) 0.0
            else {
                val sourceRec = RecFinder.findAssetRec(it.first, currYear)
                val destRec = RecFinder.findAssetRec(it.second, currYear)
                if (sourceRec == null || destRec == null) acc
                else {
                    val distribution = Math.min(acc, sourceRec.finalBalance())
                    val transfer = generateTransfer(distribution, sourceRec, destRec)
                    transferRecs.add(transfer)
                    acc - distribution
                }
            }
        }
        return transferRecs
    }
}


