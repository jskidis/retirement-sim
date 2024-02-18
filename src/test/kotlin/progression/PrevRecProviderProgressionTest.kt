package progression

import YearlyDetail
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class PrevRecProviderProgressionTest : ShouldSpec({
    val initVal = 1.0
    val prevVal = 2.0
    val nextValMult = 1.5

    val progression = PrevRecProviderProgressionFixture(initVal, prevVal, nextValMult)

    should("determineNext returns initial value when prev Year is null") {
        progression.determineNext(null).shouldBe(initVal)
    }

    should("determineNext returns prevVal times nextValMult when prevYear is provided") {
        progression.determineNext(yearlyDetailFixture())
            .shouldBeWithinPercentageOf(prevVal * nextValMult, 0.001)
    }
})

class PrevRecProviderProgressionFixture(
    val initValue: Double,
    val prevValue: Double,
    val nextValMult: Double,
) : PrevRecProviderProgression<Double> {

    override fun initialValue(): Double = initValue
    override fun previousValue(prevYear: YearlyDetail): Double = prevValue
    override fun next(prevVal: Double): Double = prevVal * nextValMult
}
