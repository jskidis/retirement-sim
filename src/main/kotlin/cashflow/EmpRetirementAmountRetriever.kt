package cashflow

import Amount
import Rate
import YearMonth
import YearlyDetail
import income.IncomeRec
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.*
import util.determineRetirementLimits

interface EmpRetirementAmountRetriever {
    fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth): Amount
    fun doProrate(): Boolean
    fun isFreeMoney(): Boolean = false
}

open class MaxAllowedAmountRetriever : EmpRetirementAmountRetriever {
    override fun doProrate(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount = determineRetirementLimits(CONTRIB_LIMIT_401K, currYear.inflation)
}

open class MaxCatchupAmountRetriever : EmpRetirementAmountRetriever {
    override fun doProrate(): Boolean = true

    override fun determineAmount(currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount =
        if (currYear.year + 1 - birthYM.toDec() <
            ConstantsProvider.getValue(RETIREMENT_CATCHUP_AGE)
        ) 0.0
        else determineRetirementLimits(CATCHUP_LIMIT_401K, currYear.inflation)
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

