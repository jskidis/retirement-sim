package expense

import YearlyDetail
import config.MainConfig

object ExpenseProcessor {
    fun process(config: MainConfig, prevYear: YearlyDetail?): List<ExpenseRec> {
        val expenses: List<ExpenseConfigProgression> = config.householdExpenses +
            config.householdMembers.people().flatMap { it.expenses() }

        return expenses.map { it.progression.determineNext(prevYear) }
    }
}