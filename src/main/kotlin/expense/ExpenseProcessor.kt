package expense

import YearlyDetail
import config.SimConfig

object ExpenseProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?): List<ExpenseRec> {
        val expenses: List<ExpenseConfigProgression> = config.household.expenses +
            config.household.members.people().flatMap { it.expenses() }

        return expenses.map { it.progression.determineNext(prevYear) }
    }
}