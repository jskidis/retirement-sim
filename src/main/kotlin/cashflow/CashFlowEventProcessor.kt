package cashflow

import RecIdentifier
import YearlyDetail
import asset.AssetChange
import asset.AssetRec
import config.SimConfig
import util.RecFinder

object CashFlowEventProcessor {
    fun process(simConfig: SimConfig, prevYear: YearlyDetail?, currYear: YearlyDetail)
    : List<AssetChange> {
        return simConfig.cashFlowConfigs(prevYear).map {
            val assetRec = findAssetRec(currYear, it.assetIdent)
            if (assetRec == null) null
            else {
                val tribution = it.handler.generateCashFlowTribution(assetRec, currYear)
                if (tribution != null) assetRec.tributions.add(tribution)
                tribution
            }
        }.mapNotNull{ it }
    }
}

private fun findAssetRec(currYear: YearlyDetail, assetIdent: RecIdentifier): AssetRec? =
    RecFinder.findAssetRec(assetIdent, currYear)
