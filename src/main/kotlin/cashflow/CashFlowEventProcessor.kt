package cashflow

import RecIdentifier
import YearlyDetail
import asset.AssetChange
import asset.AssetRec
import config.SimConfig

object CashFlowEventProcessor {
    fun process(simConfig: SimConfig, currYear: YearlyDetail): List<AssetChange> {
        return simConfig.cashFlowConfigs().map {
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
    currYear.assets.find { it.ident == assetIdent }
