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

open class MaxAllowedAmountRetriever(val pctOfMax: Rate = 1.00) : EmpRetirementAmountRetriever {
    override fun doProrate(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount = RetirementLimits.calc401kCap(currYear) * pctOfMax
}

open class MaxCatchupAmountRetriever(val pctOfMax: Rate = 1.00) : EmpRetirementAmountRetriever {
    override fun doProrate(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount = RetirementLimits.calc401kCatchup(currYear, birthYM) * pctOfMax
}

open class MaxPlusCatchupAmountRetriever(val pctOfMax: Rate = 1.00) : EmpRetirementAmountRetriever {
    override fun doProrate(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount =
        (MaxAllowedAmountRetriever(pctOfMax).determineAmount(currYear, incomeRec, birthYM) +
            MaxCatchupAmountRetriever(pctOfMax).determineAmount(currYear, incomeRec, birthYM))
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

