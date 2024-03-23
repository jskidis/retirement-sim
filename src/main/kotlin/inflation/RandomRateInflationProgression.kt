package inflation

import Rate
import YearlyDetail
import progression.PrevRecProviderProgression
import util.GaussianRndProvider

open class RandomRateInflationProgression(
    val stdMean: Rate = .0253, val stdSD: Rate = .015,
    val medMean: Rate = .0333, val medSD: Rate = .011,
    val chainMean: Rate = .0225, val chainSD: Rate = .016,
    val wageMean: Rate = .036, val wageSD: Rate = .019,
) : PrevRecProviderProgression<InflationRec>,
    GaussianRndProvider {

    override fun previousRec(prevYear: YearlyDetail): InflationRec = prevYear.inflation

    override fun initialRec(): InflationRec {
        val gaussianRnd = gaussianRndValue()
        return InflationRec(
            std = InflationRAC(rate = gaussianAdjValue(gaussianRnd, stdMean, stdSD)),
            med = InflationRAC(rate = gaussianAdjValue(gaussianRnd, medMean, medSD)),
            chain = InflationRAC(rate = gaussianAdjValue(gaussianRnd, chainMean, chainSD)),
            wage = InflationRAC(rate = gaussianAdjValue(gaussianRnd, wageMean, wageSD)),

            )
    }

    override fun nextRecFromPrev(prevYear: YearlyDetail): InflationRec =
        throw RuntimeException("Unalbe to find previous inflation rec")

    override fun nextRecFromPrev(prevRec: InflationRec, prevYear: YearlyDetail): InflationRec {
        val gaussianRnd = gaussianRndValue()

        return InflationRec(
            std = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, stdMean, stdSD),
                prev = prevRec.std
            ),
            med = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, medMean, medSD),
                prev = prevRec.med
            ),
            chain = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, chainMean, chainSD),
                prev = prevRec.chain
            ),
            wage = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, wageMean, wageSD),
                prev = prevRec.wage
            ),
        )
    }

    private fun gaussianAdjValue(rndValue: Double, mean: Double, stdDev: Double) =
        rndValue * stdDev + mean

}