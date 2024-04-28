package cashflow

import Amount
import Rate
import YearMonth
import YearlyDetail
import income.IncomeRec
import income.IncomeWithBonusRec
import util.RetirementLimits

interface EmpRetirementAmountRetriever {
    fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth): Amount
    fun isAnnualLimit(): Boolean
    fun isFreeMoney(): Boolean = false
}

open class MaxAllowedAmountRetriever(val pctOfMax: Rate = 1.00) : EmpRetirementAmountRetriever {
    override fun isAnnualLimit(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount = RetirementLimits.calc401kCap(currYear) * pctOfMax
}

open class MaxCatchupAmountRetriever(val pctOfMax: Rate = 1.00) : EmpRetirementAmountRetriever {
    override fun isAnnualLimit(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount = RetirementLimits.calc401kCatchup(currYear, birthYM) * pctOfMax
}

open class MaxPlusCatchupAmountRetriever(val pctOfMax: Rate = 1.00) : EmpRetirementAmountRetriever {
    override fun isAnnualLimit(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount =
        (MaxAllowedAmountRetriever(pctOfMax).determineAmount(currYear, incomeRec, birthYM) +
            MaxCatchupAmountRetriever(pctOfMax).determineAmount(currYear, incomeRec, birthYM))
}

open class PctOfSalaryAmountRetriever(
    val pct: Rate,
    val includeBonus: Boolean = false,
) : EmpRetirementAmountRetriever {

    override fun isAnnualLimit(): Boolean = false

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount =

        Math.min(getBonusBasedAmount(incomeRec, includeBonus) * pct,
            MaxPlusCatchupAmountRetriever().determineAmount(currYear, incomeRec, birthYM)
        )
}

open class EmployerMatchAmountRetriever(
    val pct: Rate,
    val includeBonus: Boolean = false,
) : EmpRetirementAmountRetriever {
    override fun isAnnualLimit(): Boolean = false
    override fun isFreeMoney(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount = getBonusBasedAmount(incomeRec, includeBonus) * pct
}

fun getBonusBasedAmount(incomeRec: IncomeRec, includeBonus: Boolean): Amount {
    val incomeWithBonusRec = incomeRec as? IncomeWithBonusRec
    return if (incomeWithBonusRec == null) incomeRec.amount()
        else incomeWithBonusRec.baseAmount +
            if (!includeBonus) 0.0 else incomeWithBonusRec.bonus

}

