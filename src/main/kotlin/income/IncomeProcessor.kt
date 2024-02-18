package income

import YearlyDetail
import config.MainConfig
import config.Person

object IncomeProcessor {
    fun process(config: MainConfig, prevYear: YearlyDetail?): ArrayList<IncomeRec> =
        ArrayList(config.householdMembers.people().flatMap { person: Person ->
            person.incomes().map { income ->
                income.progression.determineNext(prevYear)
            }
        })
}