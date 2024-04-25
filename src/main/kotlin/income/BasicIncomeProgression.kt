package income

import Amount
import RecIdentifier
import YearlyDetail
import progression.*
import tax.TaxabilityProfile
import util.RecFinder

open class BasicIncomeProgression(
    val ident: RecIdentifier,
    val startAmount: Amount,
    val taxabilityProfile: TaxabilityProfile,
    val adjusters: List<AmountAdjusterWithGapFiller>,
) : AmountProviderProgression<IncomeRec>,
    AmountProviderFromPrev,
    AmountToRecProvider<IncomeRec> by IncomeRecProvider(ident, taxabilityProfile),
    AmountAdjusterWithGapFiller by ChainedAmountAdjusterWithGapFiller(adjusters) {

    override fun initialAmount() = startAmount

    override fun previousAmount(prevYear: YearlyDetail): Amount? =
        RecFinder.findIncomeRec(ident, prevYear)?.baseAmount

    override fun nextAmountFromPrev(prevAmount: Amount, prevYear: YearlyDetail): Amount {
        return adjustAmount(prevAmount, prevYear)
    }

    override fun nextAmount(prevYear: YearlyDetail): Amount {
        return adjustGapFillValue(startAmount, prevYear)
    }
}


