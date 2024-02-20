package asset

import Rate
import YearlyDetail

interface AssetRORProvider {
    fun determineRate(prevYear: YearlyDetail?): Rate
}

data class RORProvider(val mean: Rate, val stdDev: Rate) : AssetRORProvider {
    override fun determineRate(prevYear: YearlyDetail?): Rate =
        mean + (stdDev * (prevYear?.rorRndGaussian ?: 0.0))
}

