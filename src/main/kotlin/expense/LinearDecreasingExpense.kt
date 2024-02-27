package expense

import Amount
import Year
import progression.*

open class LinearDecreasingExpense(
    startAmount: Amount,
    startDecYear: Year,
    numYears: Int,
    config: ExpenseConfig,
    adjusters: List<AmountAdjuster> = ArrayList(),
) : NullableAmountProviderProgression<ExpenseRec>,
    LinearDecreasingAmountProvider(startAmount, startDecYear, numYears),
    AmountToRecProvider<ExpenseRec> by ExpenseRecProvider(config),
    AmountAdjuster by ChainedAmountAdjuster(adjusters)