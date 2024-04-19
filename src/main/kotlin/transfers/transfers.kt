package transfers

import Amount
import RecIdentifier
import YearlyDetail
import asset.AssetChange
import config.SimConfig
import tax.TaxCalcConfig
import tax.TaxabilityProfile
import tax.TaxableAmounts
import toJsonStr

data class TransferRec(
    val sourceTribution: AssetChange,
    val destTribution: AssetChange
) {
    override fun toString() = toJsonStr()
}

interface TransferInfo
interface TransferGenerator {
    val sourceDestPairs: List<Pair<RecIdentifier, RecIdentifier>>
    val taxabilityProfile: TaxabilityProfile

    fun determineTransferInfo(config: SimConfig, currYear: YearlyDetail): TransferInfo?
    fun performTransfers(currYear: YearlyDetail, transferInfo: TransferInfo): List<TransferRec>
}

fun interface RothConversionAmountCalc {
    fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount
}

