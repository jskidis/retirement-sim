package income

import Amount
import YearlyDetail
import util.RandomizerFactory

fun interface BonusCalculator {
    fun calcBonus(salary: Amount, prevYear: YearlyDetail?): Amount
}

open class BonusPctByMarketRoi(val avgPct: Double, val stdDev: Double)
    : BonusCalculator {

    override fun calcBonus(salary: Amount, prevYear: YearlyDetail?): Amount {
        val pct = Math.max(0.0, avgPct + stdDev * getROIRandom(prevYear))
        return salary * pct
    }

    open fun getROIRandom(prevYear: YearlyDetail?): Double =
        RandomizerFactory.getROIRandom(prevYear)
}