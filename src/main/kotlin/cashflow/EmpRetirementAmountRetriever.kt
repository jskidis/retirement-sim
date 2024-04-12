package cashflow

import Amount
import Rate
import YearMonth
import YearlyDetail
import income.IncomeRec
import util.RetirementLimits

interface EmpRetirementAmountRetriever {
    fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth): Amount
    fun doProrate(): Boolean
    fun isFreeMoney(): Boolean = false
}

open class MaxAllowedAmountRetriever : EmpRetirementAmountRetriever {
    override fun doProrate(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount = RetirementLimits.calc401kCap(currYear)
}

open class MaxCatchupAmountRetriever : EmpRetirementAmountRetriever {
    override fun doProrate(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount = RetirementLimits.calc401kCatchup(currYear, birthYM)
}

open class MaxPlusCatchupAmountRetriever : EmpRetirementAmountRetriever {
    override fun doProrate(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount =
        MaxAllowedAmountRetriever().determineAmount(currYear, incomeRec, birthYM) +
            MaxCatchupAmountRetriever().determineAmount(currYear, incomeRec, birthYM)
}

open class PctOfSalaryAmountRetriever(
    val pct: Rate,
    val includeBonus: Boolean = false,
) : EmpRetirementAmountRetriever {

    override fun doProrate(): Boolean = false

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount =
        Math.min(
            (incomeRec.baseAmount + (if (includeBonus) incomeRec.bonus else 0.0)) * pct,
            MaxPlusCatchupAmountRetriever().determineAmount(currYear, incomeRec, birthYM)
        )
}

open class EmployerMatchAmountRetriever(
    val pct: Rate,
    val includeBonus: Boolean = false,
)
    : EmpRetirementAmountRetriever {
    override fun doProrate(): Boolean = false
    override fun isFreeMoney(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount =
        (incomeRec.baseAmount + (if (includeBonus) incomeRec.bonus else 0.0)) * pct
}

