package cashflow

import YearlyDetail
import asset.AssetChange
import asset.AssetProgression
import asset.AssetRec
import config.SimConfig

object CashFlowEventProcessor {
    fun process(simConfig: SimConfig, currYear: YearlyDetail): List<AssetChange> {
        return simConfig.assetConfigs().flatMap { asset ->
            val assetRec = findAssetRec(currYear, asset)
            if (assetRec == null) listOf()
            else {
                asset.cashflowEvents.map { it ->
                    val tribution = it.generateCashFlowTribution(assetRec, currYear)
                    if (tribution != null) assetRec.tributions.add(tribution)
                    tribution
                }.filterNotNull()
            }
        }
    }

    private fun findAssetRec(currYear: YearlyDetail, assetConfig: AssetProgression): AssetRec? =
        currYear.assets.find { it.ident == assetConfig.ident }
}