package inflation

import YearlyDetail
import util.RandomizerFactory

class StickyInflationProgression(
    val initialStickiness: Double = 0.00,
    val prevYearRatio: Double = 0.75,
    meanAndSD: InflationMeanAndSD = inflation50YearAvgs,
) : RandomRateInflationProgression(meanAndSD) {

    override fun getInflRandom(prevYear: YearlyDetail?): Double {
        return if (prevYear == null) initialStickiness
        else {
            val prevStickiness = prevYear.inflation.rndAdj
            val newRnd = RandomizerFactory.getInflRandom(prevYear)
            prevStickiness * prevYearRatio + newRnd * (1-prevYearRatio)
        }
    }
}