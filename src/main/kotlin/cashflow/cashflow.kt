package cashflow

import YearlyDetail
import asset.AssetChange
import asset.AssetRec

interface CashFlowEventHandler {
    fun generateCashFlowTribution(assetRec: AssetRec, currYear: YearlyDetail): AssetChange?
}