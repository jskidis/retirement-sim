package expense

import Amount
import RecIdentifier
import YearlyDetail
import progression.*
import tax.TaxabilityProfile

open class BasicExpenseProgression(
    val ident: RecIdentifier,
    val startAmount: Amount,
    val taxabilityProfile: TaxabilityProfile,
    val adjusters: List<AmountAdjusterWithGapFiller> = ArrayList(),
) : AmountProviderProgression<ExpenseRec>,
    AmountProviderFromPrev,
    AmountToRecProvider<ExpenseRec> by ExpenseRecProvider(ident, taxabilityProfile),
    AmountAdjusterWithGapFiller by ChainedAmountAdjusterWithGapFiller(adjusters) {

    override fun initialAmount() = startAmount

    override fun previousAmount(prevYear: YearlyDetail): Amount? =
        prevYear.expenses.find {it.ident == ident }?.amount

    override fun nextAmountFromPrev(prevAmount: Amount, prevYear: YearlyDetail): Amount =
        adjustAmount(prevAmount, prevYear)

    override fun nextAmount(prevYear: YearlyDetail): Amount =
        adjustGapFillValue(startAmount, prevYear)
}

