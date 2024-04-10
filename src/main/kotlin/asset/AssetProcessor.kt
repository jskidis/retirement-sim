package asset

import YearlyDetail
import config.SimConfig

object AssetProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?)
        : List<AssetRec> =
        config.assetConfigs().map {
            it.determineNext(prevYear)
        }.filter { it.retainRec() }
}