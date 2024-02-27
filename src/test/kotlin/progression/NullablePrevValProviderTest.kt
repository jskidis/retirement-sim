package progression

import Amount
import YearlyDetail
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class NullablePrevValProviderTest : ShouldSpec({
    val initVal = 1.0
    val prevVal = 2.0
    val nextValMult = 1.5
    val gapFillVal = 4.0

    val progression = NullablePrevValProviderFixture(
        initVal, prevVal, nextValMult, gapFillVal)

    val progressWithNullPrev = NullablePrevValProviderFixture(
        initVal, null, nextValMult, gapFillVal)

    should("determineNext returns initial value when prev Year is null") {
        progression.determineAmount(null).shouldBe(initVal)
    }

    should("determineNext returns prevVal times nextValMult when prevYear is provided") {
        progression.determineAmount(yearlyDetailFixture())
            .shouldBeWithinPercentageOf(prevVal * nextValMult, 0.001)
    }

    should("determineNext returns gapFillVal prevYear is provided but has no prev value") {
        progressWithNullPrev.determineAmount(yearlyDetailFixture())
            .shouldBe(gapFillVal)
    }
})

class NullablePrevValProviderFixture(
    val initValue: Amount,
    val prevValue: Amount?,
    val nextValMult: Amount,
    val gapFillVal: Amount,
) : NullablePrevValProvider {

    override fun initialValue(): Amount = initValue
    override fun previousValue(prevYear: YearlyDetail): Amount? = prevValue
    override fun gapFillValue(prevYear: YearlyDetail): Amount = gapFillVal
    override fun nextValue(prevVal: Amount, prevYear: YearlyDetail): Amount =
        prevValue?.let { it * nextValMult} ?: 0.0
}
