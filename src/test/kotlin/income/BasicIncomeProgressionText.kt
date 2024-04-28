package income

import Amount
import Name
import RecIdentifier
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import progression.AmountAdjusterFixtureWithGapFill
import progression.AmountAdjusterWithGapFiller
import tax.TaxabilityProfile
import tax.TaxableAmounts
import tax.WageTaxableProfile
import yearlyDetailFixture

class BasicIncomeProgressionTest : ShouldSpec({
    val prevYearMultiplier = 1.1
    val gapFillerMultipler = 2.0

    val startAmount: Amount = 1000.0
    val currAmount: Amount = startAmount * prevYearMultiplier
    val incomeName: Name = "Income Name"
    val person: Name = "Person"

    val taxableProfile = WageTaxableProfile()

    val ident = RecIdentifier(incomeName, person)
    val progression = BasicIncomeProgressionFixture(
        ident = ident,
        startAmount = startAmount,
        taxableProfile = taxableProfile,
        adjuster = AmountAdjusterFixtureWithGapFill(prevYearMultiplier, gapFillerMultipler))

    should("determineNext returns initial amount is prev year is null ") {
        val result = progression.determineNext(null)
        result.ident.name.shouldBe(incomeName)
        result.ident.person.shouldBe(person)
        result.amount().shouldBe(startAmount)
    }

    should("determineNext applies amount adjuster to previous years amount") {
        val prevYear = yearlyDetailFixture().copy(incomes = listOf(
            StdIncomeRec(year = 2024, ident = ident, amount = currAmount,
                taxableIncome = TaxableAmounts(person)
            )
        ))

        val result = progression.determineNext(prevYear)
        result.ident.name.shouldBe(incomeName)
        result.ident.person.shouldBe(person)
        result.amount().shouldBe(currAmount * prevYearMultiplier)
    }


    should("determineNext returns rec for taxable amounts based taxabilityProfile") {
        val prevYear = yearlyDetailFixture().copy(incomes = listOf(
            StdIncomeRec(year = 2024, ident = ident, amount = currAmount,
                taxableIncome = TaxableAmounts(person)
            )
        ))

        val expectedAmount = currAmount * prevYearMultiplier
        val result = progression.determineNext(prevYear)
        result.ident.name.shouldBe(incomeName)
        result.ident.person.shouldBe(person)
        result.amount().shouldBe(expectedAmount)
        result.taxable().shouldBe(taxableProfile.calcTaxable(ident.person, expectedAmount))
    }

    should("determineNext applied gaps adjustment when this income is not in previous year") {
        val prevYear = yearlyDetailFixture().copy(incomes = listOf(
            incomeRecFixture(name = incomeName, person = "Other Person", amount = 1000.0),
            incomeRecFixture(name = "Other Income", person = person, amount = 2000.0),
            incomeRecFixture(name = "Other Income", person = "Other Person", amount = 3000.0)
        ))

        val result = progression.determineNext(prevYear)
        result.ident.name.shouldBe(incomeName)
        result.ident.person.shouldBe(person)
        result.amount().shouldBe(startAmount * gapFillerMultipler)
    }
})

class BasicIncomeProgressionFixture(
    startAmount: Amount,
    ident: RecIdentifier,
    adjuster: AmountAdjusterWithGapFiller,
    taxableProfile: TaxabilityProfile
)
    : BasicIncomeProgression(ident, startAmount, taxableProfile, listOf(adjuster)
)
