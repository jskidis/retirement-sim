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
import util.RecFinder

data class TransferRec(
    val sourceRec: AssetRec,
    val sourceTribution: AssetChange,
    val destRec: AssetRec,
    val destTribution: AssetChange,
) {
    override fun toString() = toJsonStr()
}

interface TransferTaxabilityProvider {
    val taxabilityProfile: TaxabilityProfile
}

interface SourceDestPairsProvider {
    val sourceDestPairs: List<Pair<RecIdentifier, RecIdentifier>>

    companion object {
        fun multiSourceSingleDest(destAcct: RecIdentifier, sourceAccts: List<RecIdentifier>)
            : List<Pair<RecIdentifier, RecIdentifier>> = sourceAccts.map { it to destAcct }
    }
}

interface TransferAmountDeterminer : SourceDestPairsProvider {
    fun determineTransferAmount(config: SimConfig, currYear: YearlyDetail): Amount
    fun availableToTransfer(currYear: YearlyDetail) = sourceDestPairs.sumOf {
        RecFinder.findAssetRec(it.first, currYear)?.finalBalance() ?: 0.0
    }
}

interface TransferRecListGenerator : TransferTaxabilityProvider {
    fun transferName(): String
    fun generateTransfers(currYear: YearlyDetail, amount: Amount = 0.0): List<TransferRec>
}

interface TransferRecGenerator {
    fun transferName(): String
    fun generateTransfer(distribution: Amount, sourceRec: AssetRec, destRec: AssetRec): TransferRec
}

interface TransferGenerator : TransferAmountDeterminer, TransferRecListGenerator

fun interface RothConversionAmountCalc {
    fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount
}

