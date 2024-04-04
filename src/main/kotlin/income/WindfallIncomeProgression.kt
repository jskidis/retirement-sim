package income

import Amount
import Year
import YearlyDetail
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import progression.AmountProviderProgression
import progression.AmountToRecProvider
import util.yearFromPrevYearDetail

class WindfallIncomeProgression(
    val year: Year,
    val amount: Amount,
    val config: IncomeConfig,
) : AmountProviderProgression<IncomeRec>,
    AmountToRecProvider<IncomeRec> by IncomeRecProvider(config),
    CmpdInflationProvider by StdCmpdInflationProvider() {

    override fun determineAmount(prevYear: YearlyDetail?): Amount =
        if (year != yearFromPrevYearDetail(prevYear)) 0.0
        else amount * getCmpdInflationEnd(prevYear)
}