package income

import YearlyDetail
import config.Person
import config.SimConfig

object IncomeProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?): List<IncomeRec> =
        config.householdMembers.people().flatMap { person: Person ->
            person.incomes().map { income ->
                income.progression.determineNext(prevYear)
            }
        }
}