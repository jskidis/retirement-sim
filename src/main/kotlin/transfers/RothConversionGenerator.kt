package transfers

import Amount
import RecIdentifier
import YearlyDetail
import asset.AssetChange
import asset.AssetRec
import config.SimConfig
import tax.TaxabilityProfile
import util.YearBasedConfig

open class RothConversionGenerator(
    val amountCalc: YearBasedConfig<RothConversionAmountCalc>,
    override val sourceDestPairs: List<Pair<RecIdentifier, RecIdentifier>>,
    override val taxabilityProfile: TaxabilityProfile,
) : TransferGenerator {

    companion object {
        const val ROTH_CONV_STR = "RothConv"
    }

    override fun determineTransferInfo(config: SimConfig, currYear: YearlyDetail): Amount {
        val taxableAmounts = config.taxesProcessor.determineTaxableAmounts(currYear)
        val amountCalc = amountCalc.getConfigForYear(currYear.year)
        val amount = amountCalc.amountToConvert(
            currYear, taxableAmounts, config.currTaxConfig(currYear))

        val availableToConvert = sourceDestPairs.sumOf {
            findAssetRec(it.first, currYear)?.finalBalance() ?: 0.0
        }

        return Math.min(amount, availableToConvert)
    }

    override fun performTransfers(
        currYear: YearlyDetail,
        amount: Amount,
    ): List<TransferRec> {
        val transferRecs: MutableList<TransferRec> = mutableListOf()

        sourceDestPairs.fold(amount) { acc, it ->
            if (acc < 1.0) 0.0
            else {
                val sourceRec = findAssetRec(it.first, currYear)
                val destRec = findAssetRec(it.second, currYear)
                if (sourceRec == null || destRec == null) acc
                else {
                    val distribution = Math.min(acc, sourceRec.finalBalance())
                    val sourceTribution = AssetChange(
                        name = ROTH_CONV_STR,
                        amount = -distribution
                    )
                    val destTribution = AssetChange(
                        name = ROTH_CONV_STR,
                        amount = distribution,
                        taxable = taxabilityProfile.calcTaxable(
                            sourceRec.ident.person, distribution),
                        isCarryOver = true
                    )
                    transferRecs.add(TransferRec(sourceRec, sourceTribution, destRec, destTribution))
                    acc - distribution
                }
            }
        }
        return transferRecs
    }

    private fun findAssetRec(ident: RecIdentifier, currYear: YearlyDetail): AssetRec? =
        currYear.assets.find { it.ident == ident }
}


