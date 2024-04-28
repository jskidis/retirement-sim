package inflation

import Rate
import toJsonStr

interface RACProvider {
    fun racFromRec(inflation: InflationRec): InflationRAC
}

enum class INFL_TYPE : RACProvider {
    STD { override fun racFromRec(inflation: InflationRec) = inflation.std },
    MED { override fun racFromRec(inflation: InflationRec) = inflation.med },
    WAGE { override fun racFromRec(inflation: InflationRec) = inflation.wage },
}

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

    override fun toString() = toJsonStr()
}

data class InflationRec(
    val std: InflationRAC,
    val med: InflationRAC,
    val wage: InflationRAC,
    val rndAdj:Rate = 0.0
) {
    override fun toString() = toJsonStr()
}

