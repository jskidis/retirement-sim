package inflation

import YearlyDetail
import util.InflRandom
import util.RandomizerFactory

class StickyInflationProgression(
    val initialStickiness: Double = 0.00,
    val prevYearRatio: Double = 2.0/3.0,
    meanAndSD: InflationMeanAndSD = inflation50YearAvgs,
    inflRandom: InflRandom = RandomizerFactory
) : RandomRateInflationProgression(meanAndSD, inflRandom) {

    override fun getInflRandom(prevYear: YearlyDetail?): Double {
        return if (prevYear == null) initialStickiness
        else {
            val prevStickiness = prevYear.inflation.rndAdj
            val newRnd = inflRandomizer.getInflRandom(prevYear)
            prevStickiness * prevYearRatio + newRnd * (1-prevYearRatio)
        }
    }
}