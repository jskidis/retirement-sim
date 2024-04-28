package income

import Amount
import Name
import RecIdentifier
import YearMonth
import config.EmploymentConfig
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import progression.AmountAdjusterFixtureWithGapFill
import tax.TaxableAmounts
import tax.WageTaxableProfile
import util.DateRange
import yearlyDetailFixture

class EmploymentIncomeProgressionTest : ShouldSpec({
    val prevYearMultiplier = 1.05
    val gapFillerMultipler = 2.0

    val bonusPct = 0.10
    val startSalary: Amount = 100000.0
    val currSalary: Amount = startSalary * prevYearMultiplier

    val startYear = 2025
    val endYear = 2030

    val incomeName: Name = "EmployerName"
    val person: Name = "Person"

    val ident = RecIdentifier(incomeName, person)
    val amountAdjusters = listOf(
        AmountAdjusterFixtureWithGapFill(prevYearMultiplier, gapFillerMultipler)
    )

    val basePrevIncomeRec = IncomeWithBonusRec(
        year = startYear,
        ident = ident,
        baseAmount = currSalary,
        bonus = currSalary * bonusPct,
        taxableIncome = TaxableAmounts(person)
    )

    val taxableProfile = WageTaxableProfile()
    val baseEmpConfig = EmploymentConfig(
        ident = ident,
        startSalary = startSalary,
        dateRange = DateRange(YearMonth(startYear), YearMonth(endYear, 12)),
        bonusCalc = BonusByPct(bonusPct),
        taxabilityProfile = taxableProfile
    )

    should("determineNext returns initial amount is prev year is null ") {
        val progression = EmploymentIncomeProgression(baseEmpConfig, amountAdjusters)

        val result = progression.determineNext(null) as? IncomeWithBonusRec
        result.shouldNotBeNull()
        result.ident.name.shouldBe(incomeName)
        result.ident.person.shouldBe(person)
        result.baseAmount.shouldBe(startSalary)
        result.bonus.shouldBe(startSalary * bonusPct)
    }


    should("determineNext returns an zero amount rec if curr year (prev year +1) is outside of employment date range ") {
        val yearPrev = endYear
        val incomeRec = basePrevIncomeRec.copy(year = yearPrev)
        val prevYear = yearlyDetailFixture(year = yearPrev, incomes = listOf(incomeRec))

        val progression = EmploymentIncomeProgression(baseEmpConfig, amountAdjusters)
        val result = progression.determineNext(prevYear)
        result.ident.name.shouldBe(incomeName)
        result.ident.person.shouldBe(person)
        result.amount().shouldBeZero()
    }

    should("determineNext applies adjusters only to baseAmount (not amount() which is baseAmount + salary ") {
        val prevYear = yearlyDetailFixture(year = startYear, incomes = listOf(basePrevIncomeRec))
        val progression = EmploymentIncomeProgression(baseEmpConfig, amountAdjusters)

        val expectedSalary = currSalary * prevYearMultiplier
        val result = progression.determineNext(prevYear) as? IncomeWithBonusRec
        result.shouldNotBeNull()
        result.ident.name.shouldBe(incomeName)
        result.ident.person.shouldBe(person)
        result.baseAmount.shouldBe(expectedSalary)
        result.bonus.shouldBe(expectedSalary * bonusPct)
    }

    should("determineNext returns rec for taxable based taxabilityProfile that includes both salary and bonus") {
        val prevYear = yearlyDetailFixture(year = startYear, incomes = listOf(basePrevIncomeRec))
        val progression = EmploymentIncomeProgression(baseEmpConfig, amountAdjusters)

        val result = progression.determineNext(prevYear) as? IncomeWithBonusRec
        result.shouldNotBeNull()
        result.ident.name.shouldBe(incomeName)
        result.ident.person.shouldBe(person)
        result.taxable().shouldBe(taxableProfile.calcTaxable(
            ident.person, result.baseAmount + result.bonus))
    }

    should("determineNext has 0 bonus when bonusCalc is null") {
        val empConfig = baseEmpConfig.copy(bonusCalc = null)
        val incomeRec = basePrevIncomeRec.copy(bonus = 0.0)
        val prevYear = yearlyDetailFixture(year = startYear, incomes = listOf(incomeRec))
        val progression = EmploymentIncomeProgression(empConfig, amountAdjusters)

        val expectedSalary = currSalary * prevYearMultiplier
        val result = progression.determineNext(prevYear) as? IncomeWithBonusRec
        result.shouldNotBeNull()
        result.ident.name.shouldBe(incomeName)
        result.ident.person.shouldBe(person)
        result.baseAmount.shouldBe(expectedSalary)
        result.bonus.shouldBeZero()
    }
})



