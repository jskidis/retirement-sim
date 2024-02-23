package income

import YearlyDetail
import config.PersonConfig
import config.SimConfig

object IncomeProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?): List<IncomeRec> =
        config.household.members.people().flatMap { person: PersonConfig ->
            person.incomes().map { income ->
                income.progression.determineNext(prevYear)
            }
        }
}