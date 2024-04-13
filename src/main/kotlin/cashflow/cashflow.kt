package cashflow

import YearlyDetail
import asset.AssetChange
import asset.AssetRec

fun interface CashFlowEventHandler {
    fun generateCashFlowTribution(assetRec: AssetRec, currYear: YearlyDetail): AssetChange?
}