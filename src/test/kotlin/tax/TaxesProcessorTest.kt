package tax

import assetRecFixture
import config.configFixture
import expense.expenseRecFixture
import income.incomeRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class TaxesProcessorTest : ShouldSpec({
    val person = "Person1"

    val wageInc = incomeRecFixture(
        "Wage Income", person, 100000.0, WageTaxableProfile()
    )
    val fedOnlyInc = incomeRecFixture(
        "Other Income", person, 10000.0, FedOnlyTaxableProfile()
    )
    val nonDeductExp = expenseRecFixture(
        "Non Deductible Expense", person, 50000.0, NonTaxableProfile()
    )
    val decductExp = expenseRecFixture(
        "Fed Deduc Expense", person, 50000.0, FedAndStateDeductProfile()
    )
    val assetRec = assetRecFixture(
        gains = 500.0, taxProfile = NonWageTaxableProfile()
    )

    val fedTaxCalc = FixedRateTaxCalc(.10)
    val stateTaxCalc = FixedRateTaxCalc(.04)
    val socSecTaxCalc = FixedRateTaxCalc(.06)
    val medicareTaxCalc = FixedRateTaxCalc(.02)

    val config = configFixture().copy(
        taxConfig = TaxCalcConfig(fedTaxCalc, stateTaxCalc, socSecTaxCalc, medicareTaxCalc))

    should("processTaxes single wage income only no expense") {
        val currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc)
        )

        val result = TaxesProcessor.processTaxes(currYear, config)
        result.fed.shouldBe(wageInc.amount * fedTaxCalc.pct)
        result.state.shouldBe(wageInc.amount * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount * medicareTaxCalc.pct)
    }

    should("processTaxes wage and other (no payroll tax) income no expenses") {
        val currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc, fedOnlyInc)
        )

        val result = TaxesProcessor.processTaxes(currYear, config)
        result.fed.shouldBe((wageInc.amount + fedOnlyInc.amount) * fedTaxCalc.pct)
        result.state.shouldBe((wageInc.amount) * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount * medicareTaxCalc.pct)
    }

    should("processTaxes wage and other (no payroll tax) income one deductible expense and one non-deductible") {
        val currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc, fedOnlyInc),
            expenses = listOf(nonDeductExp, decductExp)
        )

        val result = TaxesProcessor.processTaxes(currYear, config)
        result.fed.shouldBe((wageInc.amount + fedOnlyInc.amount - decductExp.amount) * fedTaxCalc.pct)
        result.state.shouldBe((wageInc.amount - decductExp.amount) * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount * medicareTaxCalc.pct)
    }

    should("processTaxes wage and other (no payroll tax) income one deductible expense and one non-deductible and asset gains") {
        val currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc, fedOnlyInc),
            expenses = listOf(nonDeductExp, decductExp),
            assets = listOf(assetRec)
        )

        val result = TaxesProcessor.processTaxes(currYear, config)
        result.fed.shouldBe((wageInc.amount + fedOnlyInc.amount + assetRec.calcValues.totalGains - decductExp.amount) * fedTaxCalc.pct)
        result.state.shouldBe((wageInc.amount + assetRec.calcValues.totalGains - decductExp.amount) * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount * medicareTaxCalc.pct)
    }

})
