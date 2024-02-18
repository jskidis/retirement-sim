package inflation

import YearlyDetail
import config.MainConfig

object InflationProcessor {
    fun process(config: MainConfig, prevYear: YearlyDetail?): InflationRec =
        config.inflationConfig.determineNext(prevYear)
}