package expense

import YearlyDetail
import config.SimConfig

object ExpenseProcessor {
    fun process(config: SimConfig, prevYear: YearlyDetail?): List<ExpenseRec> =
        config.expenseConfigs().map {
            it.determineNext(prevYear)
        }.filter { it.retainRec() }
}