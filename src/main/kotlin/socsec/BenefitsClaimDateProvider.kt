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
    targetYM: YearMonth,
    val multipleOfExpense: Double,
) : BenefitsClaimDateProvider {

    val guardedTarget = maxOf(targetYM, birthYM.copy(birthYM.year + 62))
    override fun claimDate(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): YearMonth = when {
        prevYear == null || prevRec == null -> guardedTarget
        prevRec.claimDate != null -> prevRec.claimDate
        prevYear.year + 1 >= guardedTarget.year -> guardedTarget
        ageThisYear(prevYear) < 63 -> guardedTarget
        getSSIncome(prevRec.ident.person, prevYear) > prevRec.baseAmount -> guardedTarget
        assetToExpenseRatio(prevYear) >= multipleOfExpense -> guardedTarget
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
