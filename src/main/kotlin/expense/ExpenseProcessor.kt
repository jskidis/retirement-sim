package expense

import YearlyDetail
import config.SimConfig

object ExpenseProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?): List<ExpenseRec> {
        val expenses: List<ExpenseConfigProgression> = config.householdExpenses +
            config.householdMembers.people().flatMap { it.expenses() }

        return expenses.map { it.progression.determineNext(prevYear) }
    }
}