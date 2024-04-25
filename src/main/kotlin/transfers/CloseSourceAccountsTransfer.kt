package transfers

import Amount
import YearlyDetail
import util.RecFinder

interface CloseSourceAccountsTransfer :
    TransferRecListGenerator, TransferRecGenerator,
    SourceDestPairsProvider, TransferTaxabilityProvider {

    override fun generateTransfers(currYear: YearlyDetail, amount: Amount): List<TransferRec> =
        sourceDestPairs.map {
            val sourceRec = RecFinder.findAssetRec(it.first, currYear)
            val destRec = RecFinder.findAssetRec(it.second, currYear)
            if (sourceRec == null || destRec == null) null
            else generateTransfer(sourceRec.finalBalance(), sourceRec, destRec)
        }.mapNotNull { it }
}
