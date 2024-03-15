package socsec

import YearlyDetail
import config.SimConfig

object SSBenefitsProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?)
        : List<SSBenefitRec> {
        val benefits = config.household.members.people().flatMap { it.benefits }

        return benefits.map {
            it.progression.determineNext(prevYear)
        }.filter { it.retainRec() }
    }
}