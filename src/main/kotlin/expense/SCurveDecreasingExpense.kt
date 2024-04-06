package expense

import Amount
import RecIdentifier
import Year
import progression.*
import tax.TaxabilityProfile

open class SCurveDecreasingExpense(
    ident: RecIdentifier,
    startAmount: Amount,
    startDecYear: Year,
    numYears: Int,
    taxabilityProfile: TaxabilityProfile,
    adjusters: List<AmountAdjuster> = ArrayList(),
) : AmountProviderProgression<ExpenseRec>,
    SCurveDecreasingAmountProvider(startAmount, startDecYear, numYears),
    AmountToRecProvider<ExpenseRec> by ExpenseRecProvider(ident, taxabilityProfile),
    AmountAdjuster by ChainedAmountAdjuster(adjusters)