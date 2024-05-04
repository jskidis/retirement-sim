package socsec

import Rate
import YearMonth
import config.Person
import config.personFixture
import departed.DepartedRec
import inflation.CmpdInflationProviderFixture
import inflation.InflationRAC
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class SurvivorSSBenefitProgressionTest : ShouldSpec({
    val year = currentDate.year + 1
    val cmpdInflation = 2.0
    val taxablePct = 0.5

    val person = personFixture(name = "Person", birthYM = YearMonth(year - 67, 6))
    val provider = personFixture(name = "Provider", birthYM = YearMonth(year - 67, 6))

    val survivorBaseAmount = 25000.0
    val existingAdjustment = 0.75
    val newAdjustment = 0.85
    val previousClaimDate = YearMonth(year = year - 1)
    val thisYearClaimDate = YearMonth(year = year, month = 6)

    val prevSurvivorBenefitRecClaimed = benefitsRecFixture(
        year = year - 1,
        name = SurvivorSSBenefitProgression.IDENT_NAME,
        person = person.name,
        baseAmount = survivorBaseAmount,
        benefitAdjustment = existingAdjustment,
        claimDate = previousClaimDate,
    )

    val prevSurvivorBenefitRecClaimedAtZeroPct = benefitsRecFixture(
        year = year - 1,
        name = SurvivorSSBenefitProgression.IDENT_NAME,
        person = person.name,
        baseAmount = survivorBaseAmount,
        benefitAdjustment = 0.0,
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

    val prevProviderRec = benefitsRecFixture(
        year = year - 1,
        name = PrimarySSBenefitProgression.IDENT_NAME,
        person = provider.name,
        claimDate = previousClaimDate,
        baseAmount = survivorBaseAmount
    )

    fun commonRecValidation(result: SSBenefitRec) {
        result.year.shouldBe(year)
        result.ident.name.shouldBe(SurvivorSSBenefitProgression.IDENT_NAME)
        result.ident.person.shouldBe(person.name)
    }

    should("determineNext calculated amount of full benefit when prevYear has already qualified survivor benefits and has not claimed their own benefits ") {
        val prevYear = yearlyDetailFixture(year = year - 1,
            departed = listOf(DepartedRec(provider.name, year - 1)),
            benefits = listOf(prevSurvivorBenefitRecClaimed))
        val currYear = yearlyDetailFixture(year = year,
            departed = listOf(DepartedRec(provider.name, year - 1)),
            benefits = listOf()
        )

        val expectedAmount =
            survivorBaseAmount * existingAdjustment * cmpdInflation

        val results = SurvivorSSBenefitProgressionFixture(
            person = person, provider = provider, adjustment = 0.0,
            cmpdInflation = cmpdInflation, taxablePct = taxablePct
        ).determineNext(prevYear, currYear)

        commonRecValidation(results)
        results.amount.shouldBe(expectedAmount)
        results.baseAmount.shouldBe(survivorBaseAmount)
        results.benefitAdjustment.shouldBe(existingAdjustment)
        results.claimDate.shouldBe(previousClaimDate)
        results.taxableAmount.fed.shouldBe(expectedAmount * taxablePct)
    }

    should("determineNext calculated amount of full benefit minus primary when prevYear has already qualified for survivor benefits and has claimed their own benefits") {
        val prevYear = yearlyDetailFixture(year = year - 1,
            departed = listOf(DepartedRec(provider.name, year - 1)),
            benefits = listOf(prevSurvivorBenefitRecClaimed))
        val currYear = yearlyDetailFixture(year = year,
            departed = listOf(DepartedRec(provider.name, year - 1)),
            benefits = listOf(currPrimaryBenefitPrevClaimed)
        )

        val expectedAmount =
            survivorBaseAmount * existingAdjustment * cmpdInflation - currPrimaryAmount

        val results = SurvivorSSBenefitProgressionFixture(
            person = person, provider = provider, adjustment = 0.0,
            cmpdInflation = cmpdInflation, taxablePct = taxablePct
        ).determineNext(prevYear, currYear)

        commonRecValidation(results)
        results.amount.shouldBe(expectedAmount)
        results.baseAmount.shouldBe(survivorBaseAmount)
        results.benefitAdjustment.shouldBe(existingAdjustment)
        results.claimDate.shouldBe(previousClaimDate)
        results.taxableAmount.fed.shouldBe(expectedAmount * taxablePct)
    }

    should("determineNext calculated amount of new adjustment amount when prevYear has provider departed but survivor benefit pct was zero when initially 'claimed' ") {
        val prevYear = yearlyDetailFixture(year = year - 1,
            departed = listOf(DepartedRec(provider.name, year - 1)),
            benefits = listOf(prevSurvivorBenefitRecClaimedAtZeroPct))
        val currYear = yearlyDetailFixture(year = year,
            departed = listOf(DepartedRec(provider.name, year - 1)),
            benefits = listOf()
        )

        val expectedAmount =
            survivorBaseAmount * existingAdjustment * cmpdInflation

        val results = SurvivorSSBenefitProgressionFixture(
            person = person, provider = provider, adjustment = existingAdjustment,
            cmpdInflation = cmpdInflation, taxablePct = taxablePct
        ).determineNext(prevYear, currYear)

        commonRecValidation(results)
        results.amount.shouldBe(expectedAmount)
        results.baseAmount.shouldBe(survivorBaseAmount)
        results.benefitAdjustment.shouldBe(existingAdjustment)
        results.claimDate.shouldBe(YearMonth(year))
        results.taxableAmount.fed.shouldBe(expectedAmount * taxablePct)
    }


    should("determineNext calculated amount when prevYear already doesn't claimed survivor benefits but provider passed") {
        val prevYear = yearlyDetailFixture(year = year - 1,
            departed = listOf(DepartedRec(provider.name, year - 1)),
            benefits = listOf(prevProviderRec)
            )
        val currYear = yearlyDetailFixture(year = year,
            departed = listOf(DepartedRec(provider.name, year - 1)),
            benefits = listOf()
        )

        // assume claimed halfway through year
        val expectedAmount = (survivorBaseAmount * newAdjustment * cmpdInflation) * 0.5

        val results = SurvivorSSBenefitProgressionFixture(
            person = person, provider = provider, adjustment = newAdjustment,
            cmpdInflation = cmpdInflation, taxablePct = taxablePct
        ).determineNext(prevYear, currYear)

        commonRecValidation(results)
        results.amount.shouldBe(expectedAmount)
        results.baseAmount.shouldBe(survivorBaseAmount)
        results.benefitAdjustment.shouldBe(newAdjustment)
        results.claimDate.shouldBe(thisYearClaimDate)
        results.taxableAmount.fed.shouldBe(expectedAmount * taxablePct)
    }

    should("determineNext returns 0 rec when provider has not departed") {
        val prevYear = yearlyDetailFixture(year = year - 1, departed = listOf())

        val currYear = yearlyDetailFixture(
            year = year, departed = listOf(),
            benefits = listOf(currPrimaryClaimedThisYear, prevProviderRec)
        )
        val results = SurvivorSSBenefitProgressionFixture(
            person = person, provider = provider, adjustment = newAdjustment,
            cmpdInflation = cmpdInflation, taxablePct = taxablePct
        ).determineNext(prevYear, currYear)

        results.amount.shouldBeZero()
        results.baseAmount.shouldBeZero()
        results.benefitAdjustment.shouldBeZero()
        results.claimDate.shouldBeNull()
    }
})

class SurvivorSSBenefitProgressionFixture(
    person: Person,
    provider: Person,
    adjustment: Rate,
    cmpdInflation: Rate,
    taxablePct: Double = 0.5,
) : SurvivorSSBenefitProgression(
    person = person,
    provider = provider,
    taxabilityProfile = BenefitTaxableProfileFixture(taxablePct),
    benefitAdjCalc = BenefitAdjustmentCalc { _, _ -> adjustment },
    cmpdInflationProvider = CmpdInflationProviderFixture(
        InflationRAC(
            rate = .01, cmpdStart = cmpdInflation))
)
