package income

import YearlyDetail
import config.MainConfig
import config.Person

object IncomeProcessor {
    fun process(config: MainConfig, prevYear: YearlyDetail?): List<IncomeRec> =
        config.householdMembers.people().flatMap { person: Person ->
            person.incomes().map { income ->
                income.progression.determineNext(prevYear)
            }
        }
}