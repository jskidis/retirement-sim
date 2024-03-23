package expense

import Amount
import YearlyDetail
import progression.*

open class BasicExpenseProgression(
    val startAmount: Amount,
    val config: ExpenseConfig,
    val adjusters: List<AmountAdjusterWithGapFiller> = ArrayList(),
) : AmountProviderProgression<ExpenseRec>,
    AmountProviderFromPrev,
    AmountToRecProvider<ExpenseRec> by ExpenseRecProvider(config),
    AmountAdjusterWithGapFiller by ChainedAmountAdjusterWithGapFiller(adjusters) {

    override fun initialAmount() = startAmount

    override fun previousAmount(prevYear: YearlyDetail): Amount? =
        prevYear.expenses.find {
            it.config.name == config.name && it.config.person == config.person
        }?.amount

    override fun nextAmountFromPrev(prevAmount: Amount, prevYear: YearlyDetail): Amount =
        adjustAmount(prevAmount, prevYear)

    override fun nextAmount(prevYear: YearlyDetail): Amount =
        adjustGapFillValue(startAmount, prevYear)
}

