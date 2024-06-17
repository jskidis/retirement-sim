package inflation

import YearlyDetail
import progression.PrevRecProviderProgression
import util.InflRandom
import util.RandomizerFactory

open class RandomRateInflationProgression(
    val meanAndSD: InflationMeanAndSD = inflation50YearAvgs,
    val inflRandomizer: InflRandom = RandomizerFactory,
) : PrevRecProviderProgression<InflationRec> {

    override fun previousRec(prevYear: YearlyDetail): InflationRec = prevYear.inflation

    override fun initialRec(): InflationRec {
        val gaussianRnd = getInflRandom(prevYear = null)
        return InflationRec(
            std = InflationRAC(rate = gaussianAdjValue(gaussianRnd, meanAndSD.stdMean, meanAndSD.stdSD)),
            med = InflationRAC(rate = gaussianAdjValue(gaussianRnd, meanAndSD.medMean, meanAndSD.medSD)),
            wage = InflationRAC(rate = gaussianAdjValue(gaussianRnd, meanAndSD.wageMean, meanAndSD.wageSD)),
            housing = InflationRAC(rate = gaussianAdjValue(gaussianRnd, meanAndSD.housingMean, meanAndSD.housingSD)),
            rndAdj = gaussianRnd
        )
    }

    override fun nextRecFromPrev(prevYear: YearlyDetail): InflationRec =
        throw RuntimeException("Unalbe to find previous inflation rec")

    override fun nextRecFromPrev(prevRec: InflationRec, prevYear: YearlyDetail): InflationRec {
        val gaussianRnd = getInflRandom(prevYear)

        return InflationRec(
            std = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, meanAndSD.stdMean, meanAndSD.stdSD),
                prev = prevRec.std
            ),
            med = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, meanAndSD.medMean, meanAndSD.medSD),
                prev = prevRec.med
            ),
            wage = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, meanAndSD.wageMean, meanAndSD.wageSD),
                prev = prevRec.wage
            ),
            housing = InflationRAC.build(
                currRate = gaussianAdjValue(gaussianRnd, meanAndSD.housingMean, meanAndSD.housingSD),
                prev = prevRec.housing
            ),
            rndAdj = gaussianRnd
        )
    }

    private fun gaussianAdjValue(rndValue: Double, mean: Double, stdDev: Double) =
        rndValue * stdDev + mean

    open fun getInflRandom(prevYear: YearlyDetail?): Double =
        inflRandomizer.getInflRandom(prevYear)
}