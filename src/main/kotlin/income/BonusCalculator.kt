package income

import Amount
import YearlyDetail
import util.RandomizerFactory
import util.RoiRandomProvider

interface BonusCalculator {
    fun calcBonus(salary: Amount, prevYear: YearlyDetail?): Amount
}

open class BonusPctByMarketRor(val avgPct: Double, val stdDev: Double)
    : BonusCalculator, RoiRandomProvider {

    override fun calcBonus(salary: Amount, prevYear: YearlyDetail?): Amount {
        val pct = Math.max(0.0, avgPct + stdDev * getRoiRandom(prevYear))
        return salary * pct
    }

    override fun getRoiRandom(prevYear: YearlyDetail?): Double =
        RandomizerFactory.getRoiRandom(prevYear)
}