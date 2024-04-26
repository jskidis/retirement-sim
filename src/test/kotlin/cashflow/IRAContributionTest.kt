package cashflow

import YearMonth
import asset.assetRecFixture
import config.personFixture
import income.incomeRecFixture
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import tax.NonWageTaxableProfile
import tax.WageTaxableProfile
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.RETIREMENT_CATCHUP_AGE
import util.RetirementLimits
import util.currentDate
import yearlyDetailFixture

class IRAContributionTest : ShouldSpec({
    val year = currentDate.year + 1
    val contribName = "IRA-Contrib"

    val inflation = inflationRecFixture(stdRAC = InflationRAC(.03, 1.3, 1.33))
    val baseYear = yearlyDetailFixture(year, inflation)

    val catchupAge = ConstantsProvider.getValue(RETIREMENT_CATCHUP_AGE).toInt()
    val youngerPerson = personFixture("Younger", YearMonth(year - catchupAge + 5))
    val olderPerson = personFixture("Older", YearMonth(year - catchupAge - 5))

    val regIRALimit = RetirementLimits.calcIRACap(baseYear)
    val catchUpIRALimit = RetirementLimits.calcIRACatchup(baseYear, olderPerson.birthYM)

    val smallIncome = 1000.0
    val largeIncome = 100000.0
    val nonWageIncome = 20000.0

    val youngerLargeIncomeRec = incomeRecFixture(
        year, person = youngerPerson.name,
        amount = largeIncome, taxProfile = WageTaxableProfile())

    val olderNonWageIncomeRec = incomeRecFixture(
        year, person = olderPerson.name,
        amount = nonWageIncome, taxProfile = NonWageTaxableProfile())

    val olderLargeIncomeRec = incomeRecFixture(
        year, person = olderPerson.name,
        amount = largeIncome, taxProfile = WageTaxableProfile())

    val olderSmallIncomeRec = incomeRecFixture(
        year, person = olderPerson.name,
        amount = smallIncome, taxProfile = WageTaxableProfile())

    val assetRec = assetRecFixture(year)

    should("generates no cashflow event if no wage income") {
        val handler = IRAContribution(person = olderPerson, contribName = contribName,
            pctOfCap = 1.0)

        val currYear = baseYear.copy(
            incomes = listOf(youngerLargeIncomeRec, olderNonWageIncomeRec)
        )

        handler.generateCashFlowTribution(assetRec, currYear).shouldBeNull()
    }

    should("generate a cashflow event with amount no more than income even if contribution would otherwise be larger") {
        val handler = IRAContribution(person = olderPerson, contribName = contribName,
            pctOfCap = 1.0)

        val currYear = baseYear.copy(
            incomes = listOf(youngerLargeIncomeRec, olderSmallIncomeRec)
        )

        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.name.shouldBe(contribName)
        result.amount.shouldBe(smallIncome)
        result.cashflow.shouldBe(-smallIncome)
        result.taxable.shouldBeNull()
    }

    should("generate a cashflow event with amount no more than cap (not include catch up bc flag is false) if income is greater than cap") {
        val handler = IRAContribution(person = olderPerson, contribName = contribName,
            pctOfCap = 1.0, includeCatchup = false)

        val currYear = baseYear.copy(
            incomes = listOf(youngerLargeIncomeRec, olderLargeIncomeRec)
        )

        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.amount.shouldBe(regIRALimit)
        result.cashflow.shouldBe(-regIRALimit)
    }

    should("generate a cashflow event with amount no more than cap (including catch up bc flag is true) if income is greater than cap") {
        val handler = IRAContribution(person = olderPerson, contribName = contribName,
            pctOfCap = 1.0, includeCatchup = true)

        val currYear = baseYear.copy(
            incomes = listOf(youngerLargeIncomeRec, olderLargeIncomeRec)
        )

        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.amount.shouldBe(regIRALimit + catchUpIRALimit)
        result.cashflow.shouldBe(-regIRALimit - catchUpIRALimit)
    }

    should("generate a cashflow event with amount no more than cap (excluding catch up bc person is under age) if income is greater than cap") {
        val handler = IRAContribution(person = youngerPerson, contribName = contribName,
            pctOfCap = 1.0, includeCatchup = true)

        val currYear = baseYear.copy(
            incomes = listOf(youngerLargeIncomeRec)
        )

        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.amount.shouldBe(regIRALimit)
        result.cashflow.shouldBe(-regIRALimit)
    }

    should("generate a cashflow event with amount 1/2 of cap (excluding catch up bc person is under age)") {
        val handler = IRAContribution(person = youngerPerson, contribName = contribName,
            pctOfCap = 0.5, includeCatchup = false)

        val currYear = baseYear.copy(
            incomes = listOf(youngerLargeIncomeRec)
        )

        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.amount.shouldBe(0.5 * regIRALimit)
        result.cashflow.shouldBe(0.5 * -regIRALimit)
        result.taxable.shouldBeNull()
    }

    should("generate a cashflow event with amount taxable amount when taxable profile provided") {
        val handler = IRAContribution(person = youngerPerson, contribName = contribName,
            pctOfCap = 1.0, includeCatchup = false, taxabilityProfile = NonWageTaxableProfile())

        val currYear = baseYear.copy(
            incomes = listOf(youngerLargeIncomeRec)
        )

        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.name.shouldBe(contribName)
        result.amount.shouldBe(regIRALimit)
        result.cashflow.shouldBe(-regIRALimit)

        val taxableResult = result.taxable
        taxableResult.shouldNotBeNull()
        taxableResult.fed.shouldBe(regIRALimit)
        taxableResult.state.shouldBe(regIRALimit)
        taxableResult.socSec.shouldBeZero()
    }
})
