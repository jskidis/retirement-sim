package socsec

import Amount
import Rate
import RecIdentifier
import YearMonth
import YearlyDetail
import config.Person
import config.personFixture
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import tax.NonTaxableProfile
import util.currentDate
import yearlyDetailFixture

class PrimarySSBenefitProgressionTest : ShouldSpec({

    val birthYM = YearMonth(1965, 0)
    val person = personFixture("Person", birthYM)
    val ident = RecIdentifier(PrimarySSBenefitProgression.IDENT_NAME, "Person")

    val baseAmount = 1000.0
    val currentYear = currentDate.year
    val cmpndInf = 1.03
    val inflation = inflationRecFixture(stdRAC = InflationRAC(.03, 1.0, cmpndInf))

    // the benefit adjustment fixture will return fixed adjustment so birthYM doesn't matter here
    // there are sufficient tests around that calculation no need to duplicate here
    val futureYearTarget = YearMonth(2099, 0)
    val pastYearTarget = YearMonth(2020, 0)
    val currentYearTarget = YearMonth(currentYear, 6)

    val notClaimedYetRec = benefitsRecFixture(
        year = currentYear - 1, name = ident.name, person = ident.person,
        baseAmount = baseAmount, benefitAdjustment = 0.0)

    fun wasClaimedRec(adjustment: Rate) = notClaimedYetRec.copy(benefitAdjustment = adjustment)


    fun commonRecValidation(result: SSBenefitRec) {
        result.ident.shouldBe(ident)
        result.year.shouldBe(currentYear)
        result.baseAmount.shouldBe(baseAmount)
    }

    should("determineNext when prevYear is null, target is future") {
        val adjustment = 1.0
        val progression = PrimarySSBenefitProgressionFixture(
            person, futureYearTarget, baseAmount, adjustment
        )

        //note: even though fixture returns a non-zero adjustment rate, it's ignored because target is in future
        val result = progression.determineNext(null)
        commonRecValidation(result)
        result.amount.shouldBeZero()
        result.taxableAmount.total().shouldBeZero()
        result.benefitAdjustment.shouldBeZero()
    }

    should("determineNext when prevYear is null, target is past year") {
        val existingAdjustment = 1.0
        val adjustment = 1.1
        val progression = PrimarySSBenefitProgressionFixture(
            person, pastYearTarget, baseAmount, adjustment, existingAdjustment
        )

        val expectedAmount = baseAmount * existingAdjustment
        val result = progression.determineNext(null)
        commonRecValidation(result)
        result.amount.shouldBe(expectedAmount)
        result.taxableAmount.total().shouldBe(expectedAmount * 0.5)
        result.benefitAdjustment.shouldBe(existingAdjustment)
    }

    should("determineNext when prevYear is null, target is current year") {
        val adjustment = 0.9
        val progression = PrimarySSBenefitProgressionFixture(
            person, currentYearTarget, baseAmount, adjustment
        )

        val expectedAmount = (1 - currentYearTarget.monthFraction()) * baseAmount * adjustment

        val result = progression.determineNext(null)
        commonRecValidation(result)
        result.amount.shouldBe(expectedAmount)
        result.taxableAmount.total().shouldBe(expectedAmount * 0.5)
        result.benefitAdjustment.shouldBe(adjustment)
    }

    should("determineNext when prevYear not null, target is future year") {
        val progression = PrimarySSBenefitProgressionFixture(
            person, futureYearTarget, baseAmount, adjustment = 0.0
        )

        val prevYear = yearlyDetailFixture(
            currentYear - 1, inflation,
            benefits = listOf(notClaimedYetRec))

        val result = progression.determineNext(prevYear)
        commonRecValidation(result)
        result.amount.shouldBeZero()
        result.taxableAmount.total().shouldBeZero()
        result.benefitAdjustment.shouldBeZero()
    }

    should("determineNext when prevYear not null, target is past year") {
        val existingAdjustment = 1.0
        val adjustment = 1.1
        val progression = PrimarySSBenefitProgressionFixture(
            person, pastYearTarget, baseAmount, adjustment
        )

        val prevYear = yearlyDetailFixture(
            currentYear - 1, inflation,
            benefits = listOf(wasClaimedRec(existingAdjustment)))

        val expectedAmount = baseAmount * existingAdjustment * cmpndInf

        val result = progression.determineNext(prevYear)
        commonRecValidation(result)
        result.amount.shouldBe(expectedAmount)
        result.taxableAmount.total().shouldBe(
            expectedAmount *
                BenefitTaxableProfileFixture.taxablePct)
    }

    should("determineNext when prevYear not null, target is current year") {
        val adjustment = 0.9
        val progression = PrimarySSBenefitProgressionFixture(
            person, currentYearTarget.copy(year = currentYear),
            baseAmount, adjustment
        )

        val prevYear = yearlyDetailFixture(currentYear - 1, inflation)
        val expectedAmount =
            (1 - currentYearTarget.monthFraction()) * baseAmount * adjustment * cmpndInf

        val result = progression.determineNext(prevYear)
        commonRecValidation(result)
        result.amount.shouldBe(expectedAmount)
        result.taxableAmount.total().shouldBe(
            expectedAmount *
                BenefitTaxableProfileFixture.taxablePct)
        result.benefitAdjustment.shouldBe(adjustment)
    }
})

class PrimarySSBenefitProgressionFixture(
    person: Person,
    val targetYM: YearMonth,
    val baseAmount: Amount,
    val adjustment: Rate,
    val initialAdjustment: Double = 0.0,
) : PrimarySSBenefitProgression(person, BenefitTaxableProfileFixture) {

    override fun baseAmount(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): Amount = baseAmount
    override fun claimDate(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): YearMonth = targetYM
    override fun calcBenefitAdjustment(birthYM: YearMonth, startYM: YearMonth): Double = adjustment
    override fun initialAdjustment() : Rate = initialAdjustment
}

object BenefitTaxableProfileFixture : NonTaxableProfile() {
    const val taxablePct = 0.5
    override fun fed(amount: Amount): Amount = amount * taxablePct
}
