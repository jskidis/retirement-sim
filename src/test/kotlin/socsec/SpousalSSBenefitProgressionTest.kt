package socsec

import Rate
import YearMonth
import config.Person
import config.personFixture
import inflation.CmpdInflationProviderFixture
import inflation.InflationRAC
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class SpousalSSBenefitProgressionTest : ShouldSpec({
    val year = currentDate.year + 1
    val cmpdInflation = 2.0
    val taxablePct = 0.5

    val person = personFixture(name = "Person", birthYM = YearMonth(year - 67, 6))
    val spouse = personFixture(name = "Spouse", birthYM = YearMonth(year - 67, 6))

    val spouseBaseAmount = 25000.0
    val existingAdjustment = 0.40
    val newAdjustment = 0.50
    val previousClaimDate = YearMonth(year = year - 1, month = 3)
    val thisYearClaimDate = YearMonth(year = year, month = 6)

    val prevSpousalBenefitRecClaimed = benefitsRecFixture(
        year = year - 1,
        name = SpousalSSBenefitProgression.IDENT_NAME,
        person = person.name,
        baseAmount = spouseBaseAmount,
        benefitAdjustment = existingAdjustment,
        claimDate = previousClaimDate,
    )

    val currPrimaryAmount = 10000.0
    val currPrimaryBenefitPrevClaimed = benefitsRecFixture(
        year = year - 1,
        name = PrimarySSBenefitProgression.IDENT_NAME,
        person = person.name,
        claimDate = previousClaimDate,
        amount = currPrimaryAmount
    )

    val currPrimaryClaimedThisYear = currPrimaryBenefitPrevClaimed.copy(
        year = year, claimDate = thisYearClaimDate
    )
    val currPrimaryNotYetClaimed = currPrimaryClaimedThisYear.copy(claimDate = null)

    val currSpouseClaimedThisYear = benefitsRecFixture(
        year = year,
        name = PrimarySSBenefitProgression.IDENT_NAME,
        person = spouse.name,
        claimDate = previousClaimDate,
        baseAmount = spouseBaseAmount
    )
    val currSpouseNotYetClaimed = currSpouseClaimedThisYear.copy(claimDate = null)

    fun commonRecValidation(result: SSBenefitRec) {
        result.year.shouldBe(year)
        result.ident.name.shouldBe(SpousalSSBenefitProgression.IDENT_NAME)
        result.ident.person.shouldBe(person.name)
    }

    should("determineNext calculated amount when prevYear already has claimed spousal benefits") {
        val prevYear = yearlyDetailFixture(
            year = year - 1, benefits = listOf(prevSpousalBenefitRecClaimed))
        val currYear = yearlyDetailFixture(
            year = year, benefits = listOf(currPrimaryBenefitPrevClaimed)
        )

        val expectedAmount =
            spouseBaseAmount * existingAdjustment * cmpdInflation - currPrimaryAmount

        val results = SpousalSSBenefitProgressionFixture(
            person = person, spouse = spouse, adjustment = 0.0,
            cmpdInflation = cmpdInflation, taxablePct = taxablePct
        ).determineNext(prevYear, currYear)

        commonRecValidation(results)
        results.amount.shouldBe(expectedAmount)
        results.baseAmount.shouldBe(spouseBaseAmount)
        results.benefitAdjustment.shouldBe(existingAdjustment)
        results.claimDate.shouldBe(previousClaimDate)
        results.taxableAmount.fed.shouldBe(expectedAmount * taxablePct)
    }

    should("determineNext calculated amount when prevYear already doesn't claimed spousal benefits both both self and spouse claim this year") {
        val prevYear = yearlyDetailFixture(year = year - 1)
        val currYear = yearlyDetailFixture(
            year = year, benefits = listOf(
                currPrimaryClaimedThisYear, currSpouseClaimedThisYear)
        )

        val expectedAmount =
            (spouseBaseAmount * newAdjustment * cmpdInflation * 0.5 - currPrimaryAmount * 0.5)

        val results = SpousalSSBenefitProgressionFixture(
            person = person, spouse = spouse, adjustment = newAdjustment,
            cmpdInflation = cmpdInflation, taxablePct = taxablePct
        ).determineNext(prevYear, currYear)

        commonRecValidation(results)
        results.amount.shouldBe(expectedAmount)
        results.baseAmount.shouldBe(spouseBaseAmount)
        results.benefitAdjustment.shouldBe(newAdjustment)
        results.claimDate.shouldBe(thisYearClaimDate)
        results.taxableAmount.fed.shouldBe(expectedAmount * taxablePct)
    }

    should("determineNext returns 0 rec when either person or spouse has not yet claimed") {
        val prevYear = yearlyDetailFixture(year = year - 1)

        val currYearOnlyPersonClaimed = yearlyDetailFixture(
            year = year, benefits = listOf(
                currPrimaryClaimedThisYear, currSpouseNotYetClaimed)
        )
        val resultsOnlyPersonClaimed = SpousalSSBenefitProgressionFixture(
            person = person, spouse = spouse, adjustment = newAdjustment,
            cmpdInflation = cmpdInflation, taxablePct = taxablePct
        ).determineNext(prevYear, currYearOnlyPersonClaimed)

        resultsOnlyPersonClaimed.amount.shouldBeZero()
        resultsOnlyPersonClaimed.baseAmount.shouldBeZero()
        resultsOnlyPersonClaimed.benefitAdjustment.shouldBeZero()
        resultsOnlyPersonClaimed.claimDate.shouldBeNull()

        val currYearOnlySpouseClaimed = yearlyDetailFixture(
            year = year, benefits = listOf(
                currPrimaryNotYetClaimed, currSpouseClaimedThisYear)
        )
        val resultsOnlySpouseClaimed = SpousalSSBenefitProgressionFixture(
            person = person, spouse = spouse, adjustment = newAdjustment,
            cmpdInflation = cmpdInflation, taxablePct = taxablePct
        ).determineNext(prevYear, currYearOnlySpouseClaimed)

        resultsOnlySpouseClaimed.amount.shouldBeZero()
        resultsOnlySpouseClaimed.baseAmount.shouldBeZero()
        resultsOnlySpouseClaimed.benefitAdjustment.shouldBeZero()
        resultsOnlySpouseClaimed.claimDate.shouldBeNull()

        val currYearNeitherClaimed = yearlyDetailFixture(
            year = year, benefits = listOf(
                currPrimaryNotYetClaimed, currSpouseNotYetClaimed)
        )
        val resultsNeitherClaimed = SpousalSSBenefitProgressionFixture(
            person = person, spouse = spouse, adjustment = newAdjustment,
            cmpdInflation = cmpdInflation, taxablePct = taxablePct
        ).determineNext(prevYear, currYearNeitherClaimed)

        resultsNeitherClaimed.amount.shouldBeZero()
        resultsNeitherClaimed.baseAmount.shouldBeZero()
        resultsNeitherClaimed.benefitAdjustment.shouldBeZero()
        resultsNeitherClaimed.claimDate.shouldBeNull()
    }


})

class SpousalSSBenefitProgressionFixture(
    person: Person,
    spouse: Person,
    adjustment: Rate,
    cmpdInflation: Rate,
    taxablePct: Double = 0.5,
) : SpousalSSBenefitProgression(
    person = person,
    spouse = spouse,
    taxabilityProfile = BenefitTaxableProfileFixture(taxablePct),
    benefitAdjCalc = BenefitAdjustmentCalc { _, _ -> adjustment },
    cmpdInflationProvider = CmpdInflationProviderFixture(
        InflationRAC(
            rate = .01, cmpdStart = cmpdInflation))
)

