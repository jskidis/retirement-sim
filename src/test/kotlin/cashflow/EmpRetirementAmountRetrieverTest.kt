package cashflow

import YearMonth
import income.incomeRecFixture
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.*
import util.currentDate
import util.determineRetirementLimits
import yearlyDetailFixture

class EmpRetirementAmountRetrieverTest : ShouldSpec({
    val year = currentDate.year + 1
    val catchupAge = ConstantsProvider.getValue(RETIREMENT_CATCHUP_AGE).toInt()
    val birthYMUnderCatchup =YearMonth(year - catchupAge + 5)
    val birthYMOverCatchup = YearMonth(year - catchupAge - 5)

    val inflation = inflationRecFixture(stdRAC = InflationRAC(.03, 1.3, 1.33))
    val currYear = yearlyDetailFixture(year, inflation)

    val reg401kLimit = determineRetirementLimits(CONTRIB_LIMIT_401K, inflation)
    val catchUp401kLimit = determineRetirementLimits(CATCHUP_LIMIT_401K, inflation)

    val salary = 50000.0
    val bonus = 5000.0
    val incomeRec = incomeRecFixture(year = year, amount = salary, bonus = bonus)

    should("MaxAllowedAmountRetriever::determineAmount returns maximum 401k amount (no catchup)") {
        MaxAllowedAmountRetriever().determineAmount(currYear, incomeRec, birthYMOverCatchup)
            .shouldBe(reg401kLimit)
    }

    should("MaxCatchupAmountRetriever::determineAmount returns maximum catchup amount if over catchup age or 0 if not") {
        MaxCatchupAmountRetriever().determineAmount(currYear, incomeRec, birthYMOverCatchup)
            .shouldBe(catchUp401kLimit)

        MaxCatchupAmountRetriever().determineAmount(currYear, incomeRec, birthYMUnderCatchup)
            .shouldBe(0.0)
    }

    should("MaxPlusCatchupAmountRetriever::determineAmount returns maximum plus catchup (if over catchup age)") {
        MaxPlusCatchupAmountRetriever().determineAmount(currYear, incomeRec, birthYMOverCatchup)
            .shouldBe(reg401kLimit + catchUp401kLimit)

        MaxPlusCatchupAmountRetriever().determineAmount(currYear, incomeRec, birthYMUnderCatchup)
            .shouldBe(reg401kLimit)
    }

    should("PctOfSalaryAmountRetriever::determineAmount returns pct * (salary + bonus)") {
        PctOfSalaryAmountRetriever(pct = .05, includeBonus = false)
            .determineAmount(currYear, incomeRec, birthYMOverCatchup)
            .shouldBe(salary * .05)

        PctOfSalaryAmountRetriever(pct = .05, includeBonus = true)
            .determineAmount(currYear, incomeRec, birthYMOverCatchup)
            .shouldBe((salary + bonus) * .05)

        val overLimitPct = .01 + (reg401kLimit + catchUp401kLimit) / salary
        PctOfSalaryAmountRetriever(pct = overLimitPct, includeBonus = false)
            .determineAmount(currYear, incomeRec, birthYMOverCatchup)
            .shouldBe(reg401kLimit + catchUp401kLimit)
    }

    should("EmployerMatchAmountRetriever::determineAmount returns match pct * (salary + bonus)") {
        EmployerMatchAmountRetriever(pct = .05, includeBonus = false)
            .determineAmount(currYear, incomeRec, birthYMOverCatchup)
            .shouldBe(salary * .05)

        EmployerMatchAmountRetriever(pct = .05, includeBonus = true)
            .determineAmount(currYear, incomeRec, birthYMOverCatchup)
            .shouldBe((salary + bonus) * .05)

        val overLimitPct = .01 + (reg401kLimit + catchUp401kLimit) / salary
        EmployerMatchAmountRetriever(pct = overLimitPct, includeBonus = false)
            .determineAmount(currYear, incomeRec, birthYMOverCatchup)
            .shouldBe(salary * overLimitPct)
    }
})
