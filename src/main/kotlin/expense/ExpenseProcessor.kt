package expense

import YearlyDetail
import config.SimConfig
import progression.Progression

object ExpenseProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?): List<ExpenseRec> {
        val expenses: List<Progression<ExpenseRec>> = config.household.expenses +
            config.household.members.people().flatMap { it.expenses() }

        return expenses.map { it.determineNext(prevYear)
        }.filter { it.retainRec() }
    }
}