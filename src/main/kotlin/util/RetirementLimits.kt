package util

import Amount
import Year
import YearMonth
import YearlyDetail
import inflation.INFL_TYPE
import inflation.InflationRec
import util.ConstantsProvider.KEYS.*

object RetirementLimits {

    fun calc401kCap(currYear: YearlyDetail) =
        determineCap(CONTRIB_LIMIT_401K, currYear.inflation)

    fun calc401kCatchup(currYear: YearlyDetail, birthYM: YearMonth) =
        if (!isCatchUpEligible(currYear.year, birthYM)) 0.0
        else determineCap(CATCHUP_LIMIT_401K, currYear.inflation)

    fun calcIRACap(currYear: YearlyDetail) =
        determineCap(CONTRIB_LIMIT_IRA, currYear.inflation)

    fun calcIRACatchup(currYear: YearlyDetail, birthYM: YearMonth) =
        if (!isCatchUpEligible(currYear.year, birthYM)) 0.0
        else determineCap(CATCHUP_LIMIT_IRA, currYear.inflation)

    fun isCatchUpEligible(year: Year, birthYM: YearMonth): Boolean =
        year + 1 - birthYM.toDec() >= ConstantsProvider.getValue(RETIREMENT_CATCHUP_AGE)

    fun determineCap(
        key: ConstantsProvider.KEYS,
        inflation: InflationRec,
        inflType: INFL_TYPE = INFL_TYPE.STD,
        isCmpdStart: Boolean = true,
    ): Amount {
        val inflationRAC = inflType.racFromRec(inflation)
        val cmpdInflation = if (isCmpdStart) inflationRAC.cmpdStart else inflationRAC.cmpdEnd
        val unfloored = ConstantsProvider.getValue(key) * cmpdInflation
        return Math.floor(unfloored / 500.0) * 500
    }
}

