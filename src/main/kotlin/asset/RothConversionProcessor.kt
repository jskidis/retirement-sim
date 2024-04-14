package asset

import Amount
import RecIdentifier
import YearlyDetail
import config.SimConfig

object RothConversionProcessor {
    const val ROTH_CONV_STR = "RothConv"

    fun process(config: SimConfig, currYear: YearlyDetail): Amount {
        val rothConfig = config.rothConversion
        return if (rothConfig == null) 0.0
        else {
            val taxableAmounts = config.taxesProcessor.determineTaxableAmounts(currYear)
            val amountCalc = rothConfig.amountCalc.getConfigForYear(currYear.year)
            val amount = amountCalc.amountToConvert(
                currYear, taxableAmounts, config.currTaxConfig(currYear))

            val remaining = rothConfig.sourceDestPairs.fold(amount) { acc, it ->
                if (acc < 1.0) 0.0
                else {
                    val sourceRec = findAssetRec(it.first, currYear)
                    val destRec = findAssetRec(it.second, currYear)
                    if (sourceRec == null || destRec == null) acc
                    else {
                        val distribution = Math.min(acc, sourceRec.finalBalance())
                        sourceRec.tributions.add(AssetChange(
                            name = ROTH_CONV_STR,
                            amount = -distribution
                        ))
                        destRec.tributions.add(AssetChange(
                            name = ROTH_CONV_STR,
                            amount = distribution,
                            taxable = rothConfig.taxabilityProfile.calcTaxable(
                                sourceRec.ident.person, distribution),
                            isCarryOver = true
                        ))
                        acc - distribution
                    }
                }
            }
            return amount - remaining
        }
    }

    private fun findAssetRec(ident: RecIdentifier, currYear: YearlyDetail): AssetRec? =
        currYear.assets.find { it.ident == ident }
}
