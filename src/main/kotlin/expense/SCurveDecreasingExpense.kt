package expense

import Amount
import Year
import progression.AmountAdjuster
import progression.AmountToRecProvider
import progression.ChainedAmountAdjuster
import progression.SCurveDecreasingAmountProgression

class SCurveDecreasingExpense(
    startAmount: Amount,
    startDecYear: Year,
    numYears: Int,
    config: ExpenseConfig,
    adjusters: List<AmountAdjuster> = ArrayList(),
) : SCurveDecreasingAmountProgression<ExpenseRec>(startAmount, startDecYear, numYears),
    AmountToRecProvider<ExpenseRec> by ExpenseRecProvider(config),
    AmountAdjuster by ChainedAmountAdjuster(adjusters)