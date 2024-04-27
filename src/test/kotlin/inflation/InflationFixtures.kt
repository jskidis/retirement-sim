import inflation.FixedRateInflationProgression
import inflation.InflationRAC
import inflation.InflationRec

fun inflationConfigFixture(stdRate: Rate = 0.00) =
    FixedRateInflationProgression(stdRate)

fun inflationRateFixture(
    stdRate: Rate,
    medRate: Rate = stdRate,
    wageRate: Rate = stdRate,
    rndAdj: Double = 0.0
) =
    InflationRec(
        std = InflationRAC(stdRate),
        med = InflationRAC(medRate),
        wage = InflationRAC(wageRate),
        rndAdj = rndAdj
    )

fun inflationRecFixture(
    stdRAC: InflationRAC = InflationRAC(.03),
    medRAC: InflationRAC = InflationRAC(.03),
    wageRAC: InflationRAC = InflationRAC(.03),
    rndAdj: Double = 0.0
) = InflationRec(stdRAC, medRAC, wageRAC, rndAdj)

