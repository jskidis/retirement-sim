package expense

import Amount
import Year
import progression.AmountAdjuster
import progression.AmountToRecProvider
import progression.ChainedAmountAdjuster
import progression.LinearDecreasingAmountProgression

class LinearDecreasingExpense(
    startAmount: Amount,
    startDecYear: Year,
    numYears: Int,
    config: ExpenseConfig,
    adjusters: List<AmountAdjuster> = ArrayList(),
) : LinearDecreasingAmountProgression<ExpenseRec>(startAmount, startDecYear, numYears),
    AmountToRecProvider<ExpenseRec> by ExpenseRecProvider(config),
    AmountAdjuster by ChainedAmountAdjuster(adjusters)