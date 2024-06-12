package inflation

import Rate
import YearlyDetail
import progression.PrevRecProviderProgression

open class FixedRateInflationProgression(
    val fixedStdRate: Rate = .025,
    val fixedMedRate: Rate = fixedStdRate * 1.33,
    val fixedWageRate: Rate = fixedMedRate,
    val fixedHousingRate: Rate = .043
) : PrevRecProviderProgression<InflationRec> {

    override fun previousRec(prevYear: YearlyDetail): InflationRec = prevYear.inflation

    override fun nextRecFromPrev(prevRec: InflationRec, prevYear: YearlyDetail): InflationRec =
        InflationRec(
            std = InflationRAC.build(fixedStdRate, prevRec.std),
            med = InflationRAC.build(fixedMedRate, prevRec.med),
            wage = InflationRAC.build(fixedWageRate, prevRec.wage),
            housing = InflationRAC.build(fixedHousingRate, prevRec.housing)
        )

    override fun nextRecFromPrev(prevYear: YearlyDetail): InflationRec =
        throw RuntimeException("Unalbe to find previous inflation rec")

    override fun initialRec(): InflationRec =
        InflationRec(
            std = InflationRAC(fixedStdRate),
            med = InflationRAC(fixedMedRate),
            wage = InflationRAC(fixedWageRate),
            housing = InflationRAC(fixedHousingRate)
        )
}