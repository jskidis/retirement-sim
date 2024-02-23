package inflation

import YearlyDetail
import config.SimConfig

object InflationProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?): InflationRec =
        config.inflationConfig.determineNext(prevYear)
}