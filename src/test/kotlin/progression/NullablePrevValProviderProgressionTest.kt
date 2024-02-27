package progression

import Amount
import Year
import YearlyDetail
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class NullablePrevValProviderProgressionTest : ShouldSpec({
    val initVal = 1.0
    val prevVal = 2.0
    val nextValMult = 1.5
    val gapFillVal = 4.0

    val progression = NullablePrevValProviderProgressionFixture(
        initVal, prevVal, nextValMult, gapFillVal)

    val progressWithNullPrev = NullablePrevValProviderProgressionFixture(
        initVal, null, nextValMult, gapFillVal)

    should("determineNext returns initial value when prev Year is null") {
        progression.determineNext(null).shouldBe(initVal)
    }

    should("determineNext returns prevVal times nextValMult when prevYear is provided") {
        progression.determineNext(yearlyDetailFixture())
            .shouldBeWithinPercentageOf(prevVal * nextValMult, 0.001)
    }

    should("determineNext returns gapFillVal prevYear is provided but has no prev value") {
        progressWithNullPrev.determineNext(yearlyDetailFixture())
            .shouldBe(gapFillVal)
    }
})

class NullablePrevValProviderProgressionFixture(
    val initValue: Amount,
    val prevValue: Amount?,
    val nextValMult: Amount,
    val gapFillVal: Amount,
) : NullablePrevValProviderProgression<Amount> {

    override fun createRecord(value: Amount, year: Year): Amount  = value
    override fun initialValue(): Amount = initValue
    override fun previousValue(prevYear: YearlyDetail): Amount? = prevValue
    override fun gapFillValue(prevYear: YearlyDetail): Amount = gapFillVal
    override fun nextValue(prevVal: Amount, prevYear: YearlyDetail): Amount =
        prevValue?.let { it * nextValMult} ?: 0.0
}
