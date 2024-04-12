package socsec

import Amount
import RecIdentifier
import YearMonth
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import tax.NonTaxableProfile
import util.currentDate
import yearlyDetailFixture

class FixedDateAmountSSBenefitProgressionTest : ShouldSpec({

    val ident = RecIdentifier("Primary", "Person")
    val taxabilityProfile = BenefitTaxableProfileFixture()

    val baseAmount = 1000.0
    val currentYear = currentDate.year
    val cmpndInf = 1.03
    val inflation = inflationRecFixture(stdRAC = InflationRAC(.03, 1.0, cmpndInf))

    // the benefit adjustment fixture will return fixed adjustment so birthYM doesn't matter here
    // there are sufficient tests around that calculation no need to duplicate here
    val birthYM = YearMonth(1965, 0)
    val futureYearTarget = YearMonth(2099, 0)
    val pastYearTarget = YearMonth(2020, 0)
    val currentYearTarget = YearMonth(currentDate.year, 6)

    should("determineNext when prevYear is null, target is future") {
        val adjustment = 1.0
        val progression = FixedDateAmountSSBenefitProgression(
            ident, birthYM, futureYearTarget, baseAmount, taxabilityProfile,
            BenefitAdjustmentCalcFixture(adjustment)
        )

        //note: even though fixture returns a non-zero adjustment rate, it's ignored because target is in future
        val expectedAmount = 0.0
        val result = progression.determineNext(null)
        result.ident.shouldBe(ident)
        result.year.shouldBe(currentYear)
        result.amount.shouldBe(expectedAmount)
        result.taxableAmount.total().shouldBe(expectedAmount * 0.5)
    }

    should("determineNext when prevYear is null, target is past year") {
        val adjustment = 1.1
        val progression = FixedDateAmountSSBenefitProgression(
            ident, birthYM, pastYearTarget, baseAmount, taxabilityProfile,
            BenefitAdjustmentCalcFixture(adjustment)
        )

        val expectedAmount = baseAmount * adjustment
        val result = progression.determineNext(null)
        result.ident.shouldBe(ident)
        result.year.shouldBe(currentYear)
        result.amount.shouldBe(expectedAmount)
        result.taxableAmount.total().shouldBe(expectedAmount * 0.5)
    }

    should("determineNext when prevYear is null, target is current year") {
        val adjustment = 0.9
        val progression = FixedDateAmountSSBenefitProgression(
            ident, birthYM, currentYearTarget, baseAmount, taxabilityProfile,
            BenefitAdjustmentCalcFixture(adjustment)
        )

        val expectedAmount = (1 - currentYearTarget.monthFraction()) * baseAmount * adjustment

        val result = progression.determineNext(null)
        result.ident.shouldBe(ident)
        result.year.shouldBe(currentDate.year)
        result.amount.shouldBe(expectedAmount)
        result.taxableAmount.total().shouldBe(expectedAmount * 0.5)
    }

    should("determineNext when prevYear not null, target is future year") {
        val progression = FixedDateAmountSSBenefitProgression(
            ident, birthYM, futureYearTarget, baseAmount, taxabilityProfile,
            BenefitAdjustmentCalcFixture(0.0)
        )

        val prevYear = yearlyDetailFixture(currentYear, inflation)
        val expectedAmount = 0.0

        val result = progression.determineNext(prevYear)
        result.ident.shouldBe(ident)
        result.year.shouldBe(prevYear.year + 1)
        result.amount.shouldBe(expectedAmount)
        result.taxableAmount.total().shouldBe(expectedAmount * 0.5)
    }

    should("determineNext when prevYear not null, target is past year") {
        val adjustment = 1.1
        val progression = FixedDateAmountSSBenefitProgression(
            ident, birthYM, pastYearTarget, baseAmount, taxabilityProfile,
            BenefitAdjustmentCalcFixture(adjustment)
        )

        val prevYear = yearlyDetailFixture(currentYear, inflation)
        val expectedAmount = baseAmount * adjustment * cmpndInf

        val result = progression.determineNext(prevYear)
        result.ident.shouldBe(ident)
        result.year.shouldBe(prevYear.year + 1)
        result.amount.shouldBe(expectedAmount)
        result.taxableAmount.total().shouldBe(expectedAmount * 0.5)
    }

    should("determineNext when prevYear not null, target is current year") {
        val adjustment = 0.9
        val progression = FixedDateAmountSSBenefitProgression(
            ident, birthYM, currentYearTarget.copy(year = currentYear + 1), baseAmount, taxabilityProfile,
            BenefitAdjustmentCalcFixture(adjustment)
        )

        val prevYear = yearlyDetailFixture(currentYear, inflation)
        val expectedAmount =
            (1 - currentYearTarget.monthFraction()) * baseAmount * adjustment * cmpndInf

        val result = progression.determineNext(prevYear)
        result.ident.shouldBe(ident)
        result.year.shouldBe(prevYear.year + 1)
        result.amount.shouldBe(expectedAmount)
        result.taxableAmount.total().shouldBe(expectedAmount * 0.5)
    }

    should("determineNext doesn't recalc benefit adjustment after initial calc") {
        val adjustment1 = 1.0
        val adjustment2 = 1.1
        val progression = FixedDateAmountSSBenefitProgression(
            ident, birthYM, pastYearTarget, baseAmount, taxabilityProfile,
            BenefitAdjustmentCalcFixture(adjustment1, adjustment2)
        )

        val prevYear1 = yearlyDetailFixture(currentYear, inflation)
        val resultYr1 = progression.determineNext(prevYear1)
        resultYr1.amount.shouldBe(baseAmount * adjustment1 * cmpndInf)

        val prevYear2 = yearlyDetailFixture(currentYear + 1, inflation)
        val resultYr2 = progression.determineNext(prevYear2)
        resultYr2.amount.shouldBe(baseAmount * adjustment1 * cmpndInf)
    }
})

@Suppress("UNUSED_PARAMETER")
class BenefitAdjustmentCalcFixture(val adjustment1: Double, val adjustment2: Double = 0.0)
    : IBenefitAdjustmentCalc {
    var wasCalled = false

    override fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth): Double = adjustment1

    fun calcBenefitAdjustmentMulti(birthYM: YearMonth, startYM: YearMonth): Double {
        val adj = if (wasCalled) adjustment2 else adjustment1
        wasCalled = true
        return adj
    }
}

class BenefitTaxableProfileFixture : NonTaxableProfile() {
    override fun fed(amount: Amount): Amount = amount * 0.5
}
