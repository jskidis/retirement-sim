package income

import Amount
import YearlyDetail
import util.GaussianRndFromPrevYear

interface BonusCalculator {
    fun calcBonus(salary: Amount, prevYear: YearlyDetail?): Amount
}

open class BonusPctByMarketRor(val avgPct: Double, val stdDev: Double)
    : BonusCalculator, GaussianRndFromPrevYear {

    override fun calcBonus(salary: Amount, prevYear: YearlyDetail?): Amount {
        val pct = Math.max(0.0, avgPct + stdDev * gaussianRndValue(prevYear))
        return salary * pct
    }
}