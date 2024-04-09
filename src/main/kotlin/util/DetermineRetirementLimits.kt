package util

import Amount
import inflation.INFL_TYPE
import inflation.InflationRec

fun determineRetirementLimits(
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