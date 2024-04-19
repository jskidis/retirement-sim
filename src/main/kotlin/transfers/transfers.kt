package transfers

import Amount
import RecIdentifier
import YearlyDetail
import asset.AssetChange
import asset.AssetRec
import config.SimConfig
import tax.TaxCalcConfig
import tax.TaxabilityProfile
import tax.TaxableAmounts
import toJsonStr

data class TransferRec(
    val sourceRec: AssetRec,
    val sourceTribution: AssetChange,
    val destRec: AssetRec,
    val destTribution: AssetChange
) {
    override fun toString() = toJsonStr()
}

interface TransferGenerator {
    val sourceDestPairs: List<Pair<RecIdentifier, RecIdentifier>>
    val taxabilityProfile: TaxabilityProfile

    fun determineTransferInfo(config: SimConfig, currYear: YearlyDetail): Amount
    fun performTransfers(currYear: YearlyDetail, amount: Amount): List<TransferRec>
}

fun interface RothConversionAmountCalc {
    fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount
}

