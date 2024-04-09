package util

import Amount
import inflation.INFL_TYPE
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.doubles.shouldBeZero
import util.ConstantsProvider.KEYS.SS_INCOME_CAP

class DetermineRetirementLimitsKtTest : ShouldSpec({
    val stdInfl = InflationRAC(.03, 1.6, 1.63)
    val wageInfl = InflationRAC(.04, 1.8, 1.84)
    val inflationRec = inflationRecFixture(stdRAC = stdInfl, wageRAC = wageInfl)
    val baseValue = ConstantsProvider.getValue(SS_INCOME_CAP)

    fun validateResult(result: Amount, nonFloorValue: Amount) {
        (result % 500.0).shouldBeZero()
        result.shouldBeLessThan(nonFloorValue)
    }

    should("determineRetirementLimits") {
        var expected = baseValue * stdInfl.cmpdStart
        var result = determineRetirementLimits(key = SS_INCOME_CAP, inflation = inflationRec,
            inflType = INFL_TYPE.STD, isCmpdStart = true)
        validateResult(result, expected)

        expected = baseValue * stdInfl.cmpdEnd
        result = determineRetirementLimits(key = SS_INCOME_CAP, inflation = inflationRec,
            inflType = INFL_TYPE.STD, isCmpdStart = false)
        validateResult(result, expected)

        expected = baseValue * wageInfl.cmpdEnd
        result = determineRetirementLimits(key = SS_INCOME_CAP, inflation = inflationRec,
            inflType = INFL_TYPE.WAGE, isCmpdStart = true)
        validateResult(result, expected)
    }
})
