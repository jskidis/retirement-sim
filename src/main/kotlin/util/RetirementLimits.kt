package util

import Amount
import Year
import YearMonth
import YearlyDetail
import inflation.INFL_TYPE
import inflation.InflationRec
import tax.FilingStatus
import util.ConstantsProvider.KEYS.*

object RetirementLimits {

    fun calc401kCap(currYear: YearlyDetail, inflType: INFL_TYPE = INFL_TYPE.STD) =
        determineCap(CONTRIB_LIMIT_401K, currYear.inflation, inflType)

    fun calc401kCatchup(currYear: YearlyDetail, birthYM: YearMonth,
        inflType: INFL_TYPE = INFL_TYPE.STD) =
        if (!isCatchUpEligible(currYear.year, birthYM)) 0.0
        else determineCap(CATCHUP_LIMIT_401K, currYear.inflation, inflType)

    fun calcIRACap(currYear: YearlyDetail, inflType: INFL_TYPE = INFL_TYPE.STD) =
        determineCap(CONTRIB_LIMIT_IRA, currYear.inflation, inflType)

    fun calcIRACatchup(currYear: YearlyDetail, birthYM: YearMonth,
        inflType: INFL_TYPE = INFL_TYPE.STD) =
        if (!isCatchUpEligible(currYear.year, birthYM)) 0.0
        else determineCap(CATCHUP_LIMIT_IRA, currYear.inflation, inflType)

    fun isCatchUpEligible(year: Year, birthYM: YearMonth): Boolean =
        year + 1 - birthYM.toDec() >= ConstantsProvider.getValue(RETIREMENT_CATCHUP_AGE)

    fun rothIncomeLimit(currYear: YearlyDetail, inflType: INFL_TYPE = INFL_TYPE.STD): Amount {
        val key = when(currYear.filingStatus) {
            FilingStatus.JOINTLY -> ROTH_INCOME_LIMIT_JOINTLY
            else -> ROTH_INCOME_LIMIT_SINGLE
        }
        return determineCap(key, currYear.inflation, inflType)
    }

    fun determineCap(
        key: ConstantsProvider.KEYS,
        inflation: InflationRec,
        inflType: INFL_TYPE = INFL_TYPE.STD
    ): Amount {
        val cmpdInflation = inflType.racFromRec(inflation).cmpdStart
        val unfloored = ConstantsProvider.getValue(key) * cmpdInflation
        return Math.floor(unfloored / 500.0) * 500
    }
}

