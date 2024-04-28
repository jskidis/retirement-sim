package socsec

import Amount
import Name
import YearlyDetail

fun interface BenefitBaseAmountProvider {
    fun baseAmount(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): Amount
}

class StdBenefitBaseAmountProvider(val baseAmount: Amount) : BenefitBaseAmountProvider {
    override fun baseAmount(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): Amount = baseAmount
}

class NewIncomeAdjustBaseAmountProvider(
    val startAmount: Amount,
    val incPer100k: Amount
) : BenefitBaseAmountProvider {

    override fun baseAmount(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): Amount =
        if (prevRec == null || prevYear == null) startAmount
        else {
            val prevBase = prevRec.baseAmount
            val income = getSSIncome(prevRec.ident.person, prevYear)
            val increase = income / 100000.0 * incPer100k
            prevBase + increase
        }

    private fun getSSIncome(person: Name, prevYear: YearlyDetail): Amount =
        prevYear.incomes.filter { it.ident.person == person }
            .sumOf{ it.taxable().socSec }
}