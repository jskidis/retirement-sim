package expense

import Amount
import RecIdentifier
import Year
import YearlyDetail
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import progression.AmountProviderProgression
import progression.AmountToRecProvider
import tax.NonDeductProfile
import tax.TaxabilityProfile
import util.RecFinder
import util.yearFromPrevYearDetail

class HousingExpenseProgression(
    val ident: RecIdentifier,
    val annualPAndI: Amount,
    val annualTAndI: Amount,
    val rentalEquiv: Amount,
    val houseAsset: RecIdentifier,
    val paidOffYear: Year,
    val taxabilityProfile: TaxabilityProfile = NonDeductProfile(),
    val cmpdInflationProvider: CmpdInflationProvider = StdCmpdInflationProvider(),
) : AmountProviderProgression<ExpenseRec>,
    AmountToRecProvider<ExpenseRec> by ExpenseRecProvider(ident, taxabilityProfile) {

    override fun determineAmount(prevYear: YearlyDetail?): Amount =
        if (prevYear == null || stillOwnHouse(prevYear))
            cmpdInflationProvider.getCmpdInflationEnd(prevYear) * annualTAndI +
                if (housePaidOff(prevYear)) 0.0 else annualPAndI
        else {
            cmpdInflationProvider.getCmpdInflationEnd(prevYear) * rentalEquiv
        }

    private fun stillOwnHouse(prevYear: YearlyDetail): Boolean =
        RecFinder.findAssetRec(houseAsset, prevYear) != null

    private fun housePaidOff(prevYear: YearlyDetail?): Boolean =
        paidOffYear <= yearFromPrevYearDetail(prevYear)
}