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


    should("determineNext returns initial value when prev Year is null") {
        val progression = PrevRecProviderProgressionFixture(initVal, prevVal, nextValMult)
        progression.determineNext(null).shouldBe(initVal)
    }

    should("determineNext returns prevVal times nextValMult when prevYear is provided") {
        val progression = PrevRecProviderProgressionFixture(initVal, prevVal, nextValMult)
        progression.determineNext(yearlyDetailFixture())
            .shouldBeWithinPercentageOf(prevVal * nextValMult, 0.001)
    }

    should("determineNext returns 0 is previous year exists but previous record does not") {
        val progression = PrevRecProviderProgressionFixture(initVal, null, nextValMult)
        progression.determineNext(yearlyDetailFixture()).shouldBe(0.0)
    }
})

class PrevRecProviderProgressionFixture(
    val initValue: Double,
    val prevValue: Double?,
    val nextValMult: Double,
) : PrevRecProviderProgression<Double> {

    override fun previousRec(prevYear: YearlyDetail): Double? = prevValue
    override fun initialRec(): Double = initValue
    override fun nextRecFromPrev(prevYear: YearlyDetail): Double = 0.0
    override fun nextRecFromPrev(prevRec: Double, prevYear: YearlyDetail): Double = (prevValue ?: 0.0) * nextValMult
}
