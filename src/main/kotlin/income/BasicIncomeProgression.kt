package income

import Amount
import YearlyDetail
import progression.*

open class BasicIncomeProgression(
    val startAmount: Amount,
    val config: IncomeConfig,
    val adjusters: List<AmountAdjusterWithGapFiller>,
) : NullableAmountProviderProgression<IncomeRec>,
    NullablePrevValProvider,
    AmountToRecProvider<IncomeRec> by IncomeRecProvider(config),
    AmountAdjusterWithGapFiller by ChainedAmountAdjusterWithGapFiller(adjusters) {

    override fun initialValue() = startAmount

    override fun previousValue(prevYear: YearlyDetail): Amount? =
        prevYear.incomes.find {
            it.config.person == config.person && it.config.name == config.name
        }?.amount

    override fun nextValue(prevVal: Amount, prevYear: YearlyDetail): Amount {
        return adjustAmount(prevVal, prevYear)
    }

    override fun gapFillValue(prevYear: YearlyDetail): Amount {
        return adjustGapFillValue(startAmount, prevYear)
    }
}


