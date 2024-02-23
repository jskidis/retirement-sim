package asset

import Rate
import YearlyDetail
import fourDecimalFormat
import twoDecimalFormat

interface AssetRORProvider {
    fun determineRate(prevYear: YearlyDetail?): Rate
}

data class LazyPortfolioRORProvider(
    val mean: Rate, val stdDev: Rate,
    val divid: Rate, val expRatio: Rate,
    val stockPct: Rate, val bondPct: Rate, val ulcerIndex: Rate
) : AssetRORProvider {

    override fun determineRate(prevYear: YearlyDetail?): Rate =
        mean + (stdDev * (prevYear?.rorRndGaussian ?: 0.0))

    override fun toString(): String {
        return "${fourDecimalFormat.format(mean)}, ${fourDecimalFormat.format(stdDev)}, " +
            "${twoDecimalFormat.format(stockPct)}, ${twoDecimalFormat.format(bondPct)}, " +
            fourDecimalFormat.format(ulcerIndex)
    }
}

