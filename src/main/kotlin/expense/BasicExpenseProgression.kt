package expense

import Amount
import YearlyDetail
import progression.AmountAdjusterWithGapFiller
import progression.AmountToRecProvider
import progression.ChainedAmountAdjusterWithGapFiller
import progression.NullablePrevValProviderProgression

open class BasicExpenseProgression(
    val startAmount: Amount,
    val config: ExpenseConfig,
    val adjusters: List<AmountAdjusterWithGapFiller> = ArrayList(),
) : NullablePrevValProviderProgression<ExpenseRec>,
    AmountToRecProvider<ExpenseRec> by ExpenseRecProvider(config),
    AmountAdjusterWithGapFiller by ChainedAmountAdjusterWithGapFiller(adjusters) {

    override fun initialValue() = startAmount

    override fun previousValue(prevYear: YearlyDetail): Amount? =
        prevYear.expenses.find {
            it.config.name == config.name && it.config.person == config.person
        }?.amount

    override fun nextValue(prevVal: Amount, prevYear: YearlyDetail): Amount =
        adjustAmount(prevVal, prevYear)

    override fun gapFillValue(prevYear: YearlyDetail): Amount =
        adjustGapFillValue(startAmount, prevYear)
}

