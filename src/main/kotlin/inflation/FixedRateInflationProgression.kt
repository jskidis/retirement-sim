package inflation

import Rate
import YearlyDetail
import progression.PrevRecProviderProgression

open class FixedRateInflationProgression(
    val fixedStdRate: Rate = .025,
    val fixedMedRate: Rate = fixedStdRate * 1.33,
    val fixedChainedRate: Rate = fixedStdRate - 0.0025,
    val fixedWageRate: Rate = fixedMedRate,
) : PrevRecProviderProgression<InflationRec> {

    override fun previousValue(prevYear: YearlyDetail): InflationRec = prevYear.inflation

    override fun next(prevVal: InflationRec): InflationRec =
        InflationRec(
            std = InflationRAC.build(fixedStdRate, prevVal.std),
            med = InflationRAC.build(fixedMedRate, prevVal.med),
            chain = InflationRAC.build(fixedChainedRate, prevVal.chain),
            wage = InflationRAC.build(fixedWageRate, prevVal.wage),
        )

    override fun initialValue(): InflationRec =
        InflationRec(
            std = InflationRAC(fixedStdRate),
            med = InflationRAC(fixedMedRate),
            chain = InflationRAC(fixedChainedRate),
            wage = InflationRAC(fixedWageRate),
        )
}