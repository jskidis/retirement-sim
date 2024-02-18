package inflation

import GaussianRndProvider
import Rate
import YearlyDetail
import progression.PrevRecProviderProgression

open class RandomRateInflationProgression(
    val stdMean: Rate = .00253, val stdSD: Rate = .015,
    val medMean: Rate = .00333, val medSD: Rate = .011,
    val chainMean: Rate = .00225, val chainSD: Rate = .016,
    val wageMean: Rate = .0036, val wageSD: Rate = .019,
) : PrevRecProviderProgression<InflationRec>,
    GaussianRndProvider {

    override fun previousValue(prevYear: YearlyDetail): InflationRec = prevYear.inflation

    override fun initialValue(): InflationRec {
        val gaussianRnd = gaussianRndValue()
        return InflationRec(
            std = InflationRAC(rate = gaussianAdjValue(gaussianRnd, stdMean, stdSD)),
            med = InflationRAC(rate = gaussianAdjValue(gaussianRnd, medMean, medSD)),
            chain = InflationRAC(rate = gaussianAdjValue(gaussianRnd, chainMean, chainSD)),
            wage = InflationRAC(rate = gaussianAdjValue(gaussianRnd, wageMean, wageSD)),

            )
    }

    override fun next(prevVal: InflationRec): InflationRec {
        val gaussianRnd = gaussianRndValue()

        return InflationRec(
            std = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, stdMean, stdSD),
                prev = prevVal.std
            ),
            med = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, medMean, medSD),
                prev = prevVal.med
            ),
            chain = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, chainMean, chainSD),
                prev = prevVal.chain
            ),
            wage = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, wageMean, wageSD),
                prev = prevVal.wage
            ),
        )
    }

    private fun gaussianAdjValue(rndValue: Double, mean: Double, stdDev: Double) =
        rndValue * stdDev + mean

}