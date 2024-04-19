package util

import Amount
import YearMonth
import inflation.INFL_TYPE
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import tax.FilingStatus
import util.ConstantsProvider.KEYS.*
import yearlyDetailFixture

class RetirementLimitsTest : ShouldSpec({
    val stdInfl = InflationRAC(.03, 1.6, 1.63)
    val wageInfl = InflationRAC(.04, 1.8, 1.84)
    val inflationRec = inflationRecFixture(stdRAC = stdInfl, wageRAC = wageInfl)
    val baseValue = ConstantsProvider.getValue(SS_INCOME_CAP)

    val year = currentDate.year +1
    val currYear = yearlyDetailFixture(year, inflationRec, filingStatus = FilingStatus.JOINTLY)
    val currYearSingle = currYear.copy(filingStatus = FilingStatus.SINGLE)

    val catchupAge = ConstantsProvider.getValue(RETIREMENT_CATCHUP_AGE).toInt()
    val youngerPerson = YearMonth(year - catchupAge + 5)
    val olderPerson = YearMonth(year - catchupAge - 5)


    fun validateResult(result: Amount, nonFloorValue: Amount) {
        (result % 500.0).shouldBeZero()
        result.shouldBeLessThan(nonFloorValue)
    }

    should("determineCap") {
        var expected = baseValue * stdInfl.cmpdStart
        var result = RetirementLimits.determineCap(key = SS_INCOME_CAP, inflation = inflationRec,
            inflType = INFL_TYPE.STD)
        validateResult(result, expected)

        expected = baseValue * wageInfl.cmpdEnd
        result = RetirementLimits.determineCap(key = SS_INCOME_CAP, inflation = inflationRec,
            inflType = INFL_TYPE.WAGE)
        validateResult(result, expected)
    }

    should("isCatchUpEligible") {
        RetirementLimits.isCatchUpEligible(year, youngerPerson).shouldBeFalse()
        RetirementLimits.isCatchUpEligible(year, olderPerson).shouldBeTrue()
    }

    should("calc401kCap") {
        val expected = RetirementLimits.determineCap(key = CONTRIB_LIMIT_401K, inflationRec)
        RetirementLimits.calc401kCap(currYear).shouldBe(expected)
    }

    should("calcIRACap") {
        val expected = RetirementLimits.determineCap(key = CONTRIB_LIMIT_IRA, inflationRec)
        RetirementLimits.calcIRACap(currYear).shouldBe(expected)
    }

    should("calc401kCatchup") {
        val expected = RetirementLimits.determineCap(key = CATCHUP_LIMIT_401K, inflationRec)
        RetirementLimits.calc401kCatchup(currYear, youngerPerson).shouldBeZero()
        RetirementLimits.calc401kCatchup(currYear, olderPerson).shouldBe(expected)
    }

    should("calcIRACatchup") {
        val expected = RetirementLimits.determineCap(key = CATCHUP_LIMIT_IRA, inflationRec)
        RetirementLimits.calcIRACatchup(currYear, youngerPerson).shouldBeZero()
        RetirementLimits.calcIRACatchup(currYear, olderPerson).shouldBe(expected)
    }

    should("calcRothIncomeLimit") {
        val expectedJointly = RetirementLimits.determineCap(key = ROTH_INCOME_LIMIT_JOINTLY, inflationRec)
        RetirementLimits.rothIncomeLimit(currYear).shouldBe(expectedJointly)

        val expectedSingle = RetirementLimits.determineCap(key = ROTH_INCOME_LIMIT_SINGLE, inflationRec)
        RetirementLimits.rothIncomeLimit(currYearSingle).shouldBe(expectedSingle)
    }
})
