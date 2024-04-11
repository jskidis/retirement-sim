package cashflow

import Amount
import RecIdentifier
import YearMonth
import YearlyDetail
import asset.assetRecFixture
import config.EmploymentConfig
import config.personFixture
import income.IncomeRec
import income.incomeRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import tax.FedAndStateDeductProfile
import util.DateRange
import util.currentDate
import yearlyDetailFixture

class EmployerRetirementTest : ShouldSpec({
    val person = personFixture(name = "Person")
    val incomeIdent = RecIdentifier(name = "Employment", person = person.name)
    val assetIdent = RecIdentifier(name = "Asset", person = person.name)

    val year = currentDate.year +1
    val amount = 1000.0
    val salary = 50000.0
    val contribName = "Retirement-Contribution"

    val assetRec = assetRecFixture(year, assetIdent)
    val incomeRec = incomeRecFixture(year, incomeIdent.name, incomeIdent.person, salary)

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
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year +1)),
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
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year +1)),
        )
        val currYear = yearlyDetailFixture(year = year, incomes = listOf(incomeRec))
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount)

        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.name.shouldBe(contribName)
        result.amount.shouldBe(amount)
        result.cashflow.shouldBe(-amount)
    }

    should("return a cashflow event based with prorated amount retriever if employment covers part of the year and the amount retrieve indicates to prorate") {
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year, month = 6)),
        )
        val currYear = yearlyDetailFixture(year = year, incomes = listOf(incomeRec))
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount, prorate = true)

        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.name.shouldBe(contribName)
        result.amount.shouldBe(amount / 2.0)
        result.cashflow.shouldBe(-amount / 2.0)
    }

    should("return a cashflow event based non-prorated amount retriever if employment covers part of the year and the amount retrieve indicates not to prorate") {
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year, month = 6)),
        )
        val currYear = yearlyDetailFixture(year = year, incomes = listOf(incomeRec))
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount, prorate = false)

        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.name.shouldBe(contribName)
        result.amount.shouldBe(amount)
        result.cashflow.shouldBe(-amount)
    }

    should("return a cashflow event based on amount retriever but 0 cash flow if retrieve signifies its free money (emp match)") {
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year +1)),
        )
        val currYear = yearlyDetailFixture(year = year, incomes = listOf(incomeRec))
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount, freeMoney = true)

        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.name.shouldBe(contribName)
        result.amount.shouldBe(amount)
        result.cashflow.shouldBe(0.0)
    }

    should("return a cashflow event based with taxability amounts based taxability profile is not null") {
        val empConfig = EmploymentConfig(
            ident = incomeIdent, startSalary = 100000.0,
            dateRange = DateRange(start = YearMonth(year - 1), end = YearMonth(year +1)),
        )
        val currYear = yearlyDetailFixture(year = year, incomes = listOf(incomeRec))
        val amountRetriever = EmpRetirementAmountRetrieverFixture(amount = amount)

        val taxProfile = FedAndStateDeductProfile()
        val expectedTaxable = taxProfile.calcTaxable(person.name, amount)
        val handler = EmployerRetirement(empConfig, person, contribName, amountRetriever, taxProfile)
        val result = handler.generateCashFlowTribution(assetRec, currYear)
        result.shouldNotBeNull()
        result.name.shouldBe(contribName)
        result.amount.shouldBe(amount)
        result.cashflow.shouldBe(-amount)
        result.taxable.shouldBe(expectedTaxable)
    }


})

class EmpRetirementAmountRetrieverFixture(
    val amount: Amount,
    val prorate: Boolean = true,
    val freeMoney: Boolean = false,
) : EmpRetirementAmountRetriever {

    override fun determineAmount(
        currYear: YearlyDetail, incomeRec: IncomeRec, birthYM: YearMonth)
        : Amount = amount

    override fun doProrate(): Boolean = prorate
    override fun isFreeMoney(): Boolean = freeMoney
}
