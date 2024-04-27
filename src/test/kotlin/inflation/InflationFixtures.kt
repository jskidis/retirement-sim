import inflation.FixedRateInflationProgression
import inflation.InflationRAC
import inflation.InflationRec

fun inflationConfigFixture(stdRate: Rate = 0.00) =
    FixedRateInflationProgression(stdRate)

fun inflationRateFixture(
    stdRate: Rate, medRate: Rate = stdRate, wageRate: Rate = stdRate,
) =
    InflationRec(
        InflationRAC(stdRate),
        InflationRAC(medRate),
        InflationRAC(wageRate),
    )

fun inflationRecFixture(
    stdRAC: InflationRAC = InflationRAC(.03),
    medRAC: InflationRAC = InflationRAC(.03),
    wageRAC: InflationRAC = InflationRAC(.03),
) = InflationRec(stdRAC, medRAC, wageRAC)

