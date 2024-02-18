package expense

import YearlyDetail
import config.MainConfig

object ExpenseProcessor {
    fun process(config: MainConfig, prevYear: YearlyDetail?): ArrayList<ExpenseRec> {
        val expenses: List<ExpenseConfigProgression> = config.householdExpenses +
            config.householdMembers.people().flatMap { it.expenses() }

        return ArrayList(expenses.map { it.progression.determineNext(prevYear) })
    }
}