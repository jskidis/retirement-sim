package income

import Amount
import RecIdentifier
import Year
import YearlyDetail
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import progression.AmountProviderProgression
import progression.AmountToRecProvider
import tax.TaxabilityProfile
import util.yearFromPrevYearDetail

class WindfallIncomeProgression(
    val ident: RecIdentifier,
    val year: Year,
    val amount: Amount,
    val taxabilityProfile: TaxabilityProfile,
) : AmountProviderProgression<IncomeRec>,
    AmountToRecProvider<IncomeRec> by IncomeRecProvider(ident, taxabilityProfile),
    CmpdInflationProvider by StdCmpdInflationProvider() {

    override fun determineAmount(prevYear: YearlyDetail?): Amount =
        if (year != yearFromPrevYearDetail(prevYear)) 0.0
        else amount * getCmpdInflationEnd(prevYear)
}