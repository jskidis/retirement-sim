package income

import Amount
import YearlyDetail
import progression.*

open class BasicIncomeProgression(
    val startAmount: Amount,
    val config: IncomeConfig,
    val adjusters: List<AmountAdjusterWithGapFiller>,
) : AmountProviderProgression<IncomeRec>,
    AmountProviderFromPrev,
    AmountToRecProvider<IncomeRec> by IncomeRecProvider(config),
    AmountAdjusterWithGapFiller by ChainedAmountAdjusterWithGapFiller(adjusters) {

    override fun initialAmount() = startAmount

    override fun previousAmount(prevYear: YearlyDetail): Amount? =
        prevYear.incomes.find {
            it.config.person == config.person && it.config.name == config.name
        }?.amount

    override fun nextAmountFromPrev(prevAmount: Amount, prevYear: YearlyDetail): Amount {
        return adjustAmount(prevAmount, prevYear)
    }

    override fun nextAmount(prevYear: YearlyDetail): Amount {
        return adjustGapFillValue(startAmount, prevYear)
    }
}


