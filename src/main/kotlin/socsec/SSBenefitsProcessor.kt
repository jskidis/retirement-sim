package socsec

import YearlyDetail
import config.SimConfig

object SSBenefitsProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?)
        : List<SSBenefitRec> =

        config.primaryBenefitsConfigs().map {
            it.determineNext(prevYear)
        }.filter {it.retainRec() }

    fun processSecondary(config: SimConfig, prevYear: YearlyDetail?, currYear: YearlyDetail)
        : List<SSBenefitRec> =

        config.secondaryBenefitsConfigs().map {
            it.determineNext(prevYear, currYear)
        }.filter { it.retainRec() }
}