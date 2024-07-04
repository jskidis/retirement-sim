package cashflow

import Amount
import RecIdentifier
import YearMonth
import YearlyDetail
import asset.AssetChange
import asset.assetRecFixture
import config.EmploymentConfig
import config.personFixture
import income.IncomeRec
import income.incomeRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import tax.FedAndStateDeductProfile
import tax.TaxableAmounts
import util.DateRange
import util.currentDate
import yearlyDetailFixture

class EmployerRetirementTest : ShouldSpec({
    val person = personFixture(name = "Person")
    val incomeIdent = RecIdentifier(name = "Employment", person = person.name)
    val assetIdent = RecIdentifier(name = "Asset", person = person.name)

    val year = currentDate.year + 1
    val amount = 1000.0
    val salary = 50000.0
    val contribName = "Retirement-Contribution"

    val assetRec = assetRecFixture(year, assetIdent)
    val incomeRec = incomeRecFixture(year, incomeIdent.name, incomeIdent.person, salary)

    fun validateCashFlowRec(
        result: AssetChange, amount: Amount, cashFlowAmount: Amount = -amount,
        taxable: TaxableAmounts? = null, hasAccruedAmount: Boolean = false
    ) {
        result.name.shouldBe(contribName)
        result.amount.shouldBe(amount)
        result.taxable.shouldBe(taxable)

        if (!hasAccruedAmount) {
            result.cashflow.shouldBe(cashFlowAmount)
            result.accruedAmt.shouldBeZero()
        }
        else {
            result.accruedAmt.shouldBeGreaterThan(0.0).shouldBeLessThan(amount)
        }
    }

    should("not create a cashflow event if employment dates don't involve this year") {
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(2000), end = YearMonth(2001)),
        )
        val currYear = yearlyDetailFixture(year = year)
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount)

        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldBeNull()
    }

    should("not create a cashflow event if income rec not found") {
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year + 1)),
        )
        val currYear = yearlyDetailFixture(year = year)
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount)

        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldBeNull()
    }

    should("return a cashflow event based on amount retriever if employment covers all year") {
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year + 1)),
        )
        val currYear = yearlyDetailFixture(year = year, incomes = listOf(incomeRec))
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount)

        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        validateCashFlowRec(result, amount)
    }

    should("return a cashflow event based with prorated amount retriever if employment covers part of the year and the amount retrieve indicates to prorate") {
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year, month = 6)),
        )
        val currYear = yearlyDetailFixture(year = year, incomes = listOf(incomeRec))
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount, annualLimit = true)

        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        validateCashFlowRec(result, amount / 2.0)
    }

    should("return a cashflow event based non-prorated amount retriever if employment covers part of the year and the amount retrieve indicates not to prorate") {
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year, month = 6)),
        )
        val currYear = yearlyDetailFixture(year = year, incomes = listOf(incomeRec))
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount, annualLimit = false)

        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        validateCashFlowRec(result, amount)
    }

    should("return a cashflow event based on amount retriever but 0 cash flow if retrieve signifies its free money (emp match)") {
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year + 1)),
        )
        val currYear = yearlyDetailFixture(year = year, incomes = listOf(incomeRec))
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount, freeMoney = true)

        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        validateCashFlowRec(result, amount, cashFlowAmount = 0.0)
    }

    should("return a cashflow event based with taxability amounts based taxability profile is not null") {
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year + 1)),
        )
        val currYear = yearlyDetailFixture(year = year, incomes = listOf(incomeRec))
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount)

        val taxProfile = FedAndStateDeductProfile()
        val expectedTaxable = taxProfile.calcTaxable(person.name, amount)
        val handler =
            EmployerRetirement(empConfig, person, contribName, amountRetriever, taxProfile)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        validateCashFlowRec(result, amount, taxable = expectedTaxable)
    }

    should("return that the event has been partially accrued if year is the actual current year") {
        val yearAsCurr = currentDate.year
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(yearAsCurr - 1), end = YearMonth(yearAsCurr + 1)),
        )
        val currYear = yearlyDetailFixture(year = yearAsCurr, incomes = listOf(incomeRec))
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount)

        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        validateCashFlowRec(result, amount, hasAccruedAmount = true)
    }

})

class EmpRetirementAmountRetrieverFixture(
    val amount: Amount,
    val annualLimit: Boolean = true,
    val freeMoney: Boolean = false,
) : EmpRetirementAmountRetriever {

    override fun determineAmount(
        currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth,
    ) : Amount = amount

    override fun isAnnualLimit(): Boolean = annualLimit
    override fun isFreeMoney(): Boolean = freeMoney
}
