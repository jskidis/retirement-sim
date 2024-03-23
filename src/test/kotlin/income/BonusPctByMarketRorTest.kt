package income

import YearlyDetail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class BonusPctByMarketRorTest : FunSpec({

    val stdPct = .1
    val stdDev = .05
    val salary = 10000.0

    test("calcBonus is fixed at avg pct when gaussianRnd is 0.0") {
        val bonusCalc = BonusPctByMarketRorFixture(stdPct, stdDev, 0.0)
        bonusCalc.calcBonus(salary, null).shouldBe(salary * stdPct)
    }

    test("calcBonus is impacted by market (gaussian rnd") {
        val bonusCalc = BonusPctByMarketRorFixture(stdPct, stdDev, 2.0)
        bonusCalc.calcBonus(salary, null).shouldBe(salary * (stdPct + stdDev * 2.0))
    }

    test("calcBonus is never below zero") {
        val bonusCalc = BonusPctByMarketRorFixture(stdPct, stdDev, -100.0)
        bonusCalc.calcBonus(salary, null).shouldBe(0.0)
    }

})

class BonusPctByMarketRorFixture(avgPct: Double, stdDev: Double, val gaussianRnd: Double)
    : BonusPctByMarketRor(avgPct, stdDev) {
    override fun gaussianRndValue(prevYear: YearlyDetail?): Double = gaussianRnd
}
