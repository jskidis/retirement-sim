package income

import YearlyDetail
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class BonusPctByMarketROITest : ShouldSpec({

    val stdPct = .1
    val stdDev = .05
    val salary = 10000.0

    should("calcBonus is fixed at avg pct when gaussianRnd is 0.0") {
        val bonusCalc = BonusPctByMarketROIFixture(stdPct, stdDev, 0.0)
        bonusCalc.calcBonus(salary, null).shouldBe(salary * stdPct)
    }

    should("calcBonus is impacted by market (gaussian rnd") {
        val bonusCalc = BonusPctByMarketROIFixture(stdPct, stdDev, 2.0)
        bonusCalc.calcBonus(salary, null).shouldBe(salary * (stdPct + stdDev * 2.0))
    }

    should("calcBonus is never below zero") {
        val bonusCalc = BonusPctByMarketROIFixture(stdPct, stdDev, -100.0)
        bonusCalc.calcBonus(salary, null).shouldBe(0.0)
    }

})

class BonusPctByMarketROIFixture(avgPct: Double, stdDev: Double, val gaussianRnd: Double)
    : BonusPctByMarketRoi(avgPct, stdDev) {
    override fun getROIRandom(prevYear: YearlyDetail?): Double = gaussianRnd
}
