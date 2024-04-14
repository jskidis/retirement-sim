package socsec

import YearlyDetail
import config.SimConfig

object SSBenefitsProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?)
        : List<SSBenefitRec> {
        val benefits = config.household.members.people().flatMap { it.benefits() }
        return benefits.map { it.determineNext(prevYear) }.filter { it.retainRec() }
    }

    fun processSecondary(config: SimConfig, prevYear: YearlyDetail?, currYear: YearlyDetail)
        : List<SSBenefitRec> {
        val benefits = config.household.members.people().flatMap { it.secondaryBenefits() }
        return benefits.map { it.determineNext(prevYear, currYear) }.filter { it.retainRec() }
    }
}