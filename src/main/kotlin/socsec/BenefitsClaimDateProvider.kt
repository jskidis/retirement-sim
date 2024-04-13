package socsec

import Amount
import Name
import YearMonth
import YearlyDetail

fun interface BenefitsClaimDateProvider {
    fun claimDate(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): YearMonth
}

class StdBenefitsClaimDateProvider(val targetYM: YearMonth) : BenefitsClaimDateProvider {
    override fun claimDate(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): YearMonth = targetYM
}

class FlexibleClaimDateProvider(
    val birthYM: YearMonth,
    val targetYM: YearMonth,
    val multipleOfExpense: Double,
) : BenefitsClaimDateProvider {

    override fun claimDate(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): YearMonth = when {
        prevYear == null || prevRec == null -> targetYM
        prevRec.claimDate != null -> prevRec.claimDate
        prevYear.year + 1 >= targetYM.year -> targetYM
        ageThisYear(prevYear) < 63 -> targetYM
        getSSIncome(prevRec.ident.person, prevYear) > prevRec.baseAmount -> targetYM
        assetToExpenseRatio(prevYear) >= multipleOfExpense -> targetYM
        else -> YearMonth(year = prevYear.year + 1, 0)
    }

    private fun ageThisYear(prevYear: YearlyDetail): Int =
        prevYear.year + 1 - birthYM.year

    private fun getSSIncome(person: Name, prevYear: YearlyDetail): Amount =
        prevYear.incomes.filter { it.ident.person == person }
            .sumOf{ it.taxableIncome.socSec }

    private fun assetToExpenseRatio(prevYear: YearlyDetail): Amount =
        prevYear.totalAssetValues() / (prevYear.totalExpense() - prevYear.totalBenefits())
}
