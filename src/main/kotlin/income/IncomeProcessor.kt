package income

import YearlyDetail
import config.ConfigConstants
import config.PersonConfig
import config.SimConfig

object IncomeProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?): List<IncomeRec> =
        config.household.members.people().flatMap { person: PersonConfig ->
            person.incomes().map { income ->
                capSocSecTaxableIncome(
                    income.progression.determineNext(prevYear), prevYear)
            }.filter { it.retainRec() }
        }

    private fun capSocSecTaxableIncome(incomeRec: IncomeRec, prevYear: YearlyDetail?): IncomeRec {
        val cap = ConfigConstants.socSecIncomeCapBase * (prevYear?.inflation?.wage?.cmpdEnd ?: 1.0)
        val roundedCap = (Math.round(cap / 100.0)) * 100.0
        return if (incomeRec.taxableIncome.socSec <= roundedCap) incomeRec
        else incomeRec.copy(taxableIncome = incomeRec.taxableIncome.copy(socSec = roundedCap))
    }
}