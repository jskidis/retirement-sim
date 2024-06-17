package expense

import Amount
import RecIdentifier
import YearlyDetail
import departed.ActuarialEvent
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import progression.AmountProviderProgression
import progression.AmountToRecProvider
import tax.NonDeductProfile
import tax.TaxabilityProfile

class EventBasedExpenseProgression(
    val ident: RecIdentifier,
    val amount: Amount,
    val eventCalc: ActuarialEvent,
    val taxabilityProfile: TaxabilityProfile = NonDeductProfile(),
    val cmpdInflationProvider: CmpdInflationProvider = StdCmpdInflationProvider(),
) : AmountProviderProgression<ExpenseRec>,
    AmountToRecProvider<ExpenseRec> by ExpenseRecProvider(ident, taxabilityProfile) {

    override fun determineAmount(prevYear: YearlyDetail?): Amount =
        if (prevYear == null || !eventCalc.didEventOccur(prevYear)) 0.0
        else {
            cmpdInflationProvider.getCmpdInflationEnd(prevYear) * amount
        }
}