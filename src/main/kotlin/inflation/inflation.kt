package inflation

import Rate

data class InflationRAC(
    val rate: Rate,
    val cmpdStart: Rate = 1.0,
    val cmpdEnd: Rate = cmpdStart * (1.0 + rate),
) {
    companion object {
        fun build(currRate: Rate, prev: InflationRAC) = InflationRAC(
            rate = currRate,
            cmpdStart = prev.cmpdStart * (1.0 + currRate),
            cmpdEnd = prev.cmpdEnd * (1.0 + currRate)
        )
    }
}

data class InflationRec(
    val std: InflationRAC,
    val med: InflationRAC,
    val chain: InflationRAC,
    val wage: InflationRAC,
)

