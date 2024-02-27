package expense

import Amount
import Year
import progression.*

open class SCurveDecreasingExpense(
    startAmount: Amount,
    startDecYear: Year,
    numYears: Int,
    config: ExpenseConfig,
    adjusters: List<AmountAdjuster> = ArrayList(),
) : AmountProviderProgression<ExpenseRec>,
    SCurveDecreasingAmountProvider(startAmount, startDecYear, numYears),
    AmountToRecProvider<ExpenseRec> by ExpenseRecProvider(config),
    AmountAdjuster by ChainedAmountAdjuster(adjusters)