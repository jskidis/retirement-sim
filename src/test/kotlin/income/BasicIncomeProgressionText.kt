package income

import Amount
import Name
import RecIdentifier
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import progression.AmountAdjusterWithGapFiller
import progression.GapAmountAdjusterFixture
import tax.TaxableAmounts
import tax.WageTaxableProfile
import yearlyDetailFixture

class BasicIncomeProgressionTest : ShouldSpec({
    val startAmount: Amount = 1000.0
    val incomeName: Name = "Income Name"
    val person: Name = "Person"
    val prevYearMultiplier = 1.1
    val gapFillerMultipler = 2.0

    val ident = RecIdentifier(incomeName, person)
    val progression = BasicIncomeProgressionFixture(
        ident = ident,
        startAmount = startAmount,
        adjuster = GapAmountAdjusterFixture(prevYearMultiplier, gapFillerMultipler))

    should("determineNext returns initial amount is prev year is null ") {
        val result = progression.determineNext(null)
        result.config.name.shouldBe(incomeName)
        result.config.person.shouldBe(person)
        result.amount().shouldBe(startAmount)
    }

    should("determineNext applies amount adjuster to previous years amount") {
        val prevYear = yearlyDetailFixture().copy(incomes = listOf(
            IncomeRec(year = 2024, ident = ident, baseAmount = 2000.0,
                taxableIncome = TaxableAmounts(person)
            )
        ))

        val result = progression.determineNext(prevYear)
        result.config.name.shouldBe(incomeName)
        result.config.person.shouldBe(person)
        result.amount().shouldBe(2000.0 * prevYearMultiplier)
    }

    should("determineNext applied gaps adjustment when this expense is not in previous year") {
        val prevYear = yearlyDetailFixture().copy(incomes = listOf(
            incomeRecFixture(name = incomeName, person = "Other Person", amount = 1000.0),
            incomeRecFixture(name = "Other Expense", person = person, amount = 2000.0),
            incomeRecFixture(name = "Other Expense", person = "Other Person", amount = 3000.0)
        ))

        val result = progression.determineNext(prevYear)
        result.config.name.shouldBe(incomeName)
        result.config.person.shouldBe(person)
        result.amount().shouldBe(startAmount * gapFillerMultipler)
    }
})

class BasicIncomeProgressionFixture(
    startAmount: Amount,
    ident: RecIdentifier,
    adjuster: AmountAdjusterWithGapFiller
)
    : IncomeProgression(ident, startAmount, WageTaxableProfile(), listOf(adjuster)
)
