package cashflow

import RecIdentifier
import YearlyDetail
import asset.AssetChange
import asset.AssetRec

data class CashFlowEventConfig (
    val assetIdent: RecIdentifier,
    val handler: CashFlowEventHandler
)

fun interface CashFlowEventHandler {
    fun generateCashFlowTribution(assetRec: AssetRec, currYear: YearlyDetail): AssetChange?
}
