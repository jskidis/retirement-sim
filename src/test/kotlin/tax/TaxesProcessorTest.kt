package tax

import asset.assetRecFixture
import config.configFixture
import expense.expenseRecFixture
import income.incomeRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.STD_DEDUCT_JOINTLY
import yearlyDetailFixture

class TaxesProcessorTest : ShouldSpec({
    val person = "Person1"

    val wageInc = incomeRecFixture(
        2024, "Wage Income", person, 100000.0, WageTaxableProfile()
    )
    val fedOnlyInc = incomeRecFixture(
        2024, "Other Income", person, 10000.0, FedOnlyTaxableProfile()
    )
    val nonDeductExp = expenseRecFixture(
        2024, "Non Deductible Expense", person, 50000.0, NonTaxableProfile()
    )
    val decductExp = expenseRecFixture(
        2024, "Fed Deduc Expense", person, 50000.0, FedAndStateDeductProfile()
    )
    val assetRec = assetRecFixture(
        gains = 500.0, taxProfile = NonWageTaxableProfile()
    )

    val fedTaxRate = .10
    val fedTaxCalc = BracketTaxCalcFixture(fedTaxRate)
    val fedLTGTaxRate = .05
    val fedLTGTaxCalc = BracketTaxCalcFixture(fedLTGTaxRate)

    val stateTaxCalc = FixedRateTaxCalc(.04)
    val socSecTaxCalc = FixedRateTaxCalc(.06)
    val medicareTaxCalc = FixedRateTaxCalc(.02)

    val filingStatus = FilingStatus.JOINTLY
    val stdDeduct = ConstantsProvider.getValue(STD_DEDUCT_JOINTLY)

    val config = configFixture().copy(
        taxConfig = TaxCalcConfig(
            fedTaxCalc, fedLTGTaxCalc, stateTaxCalc, socSecTaxCalc, medicareTaxCalc))

    should("processTaxes single wage income only no expense") {
        val currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc), filingStatus = filingStatus
        )

        val result = TaxesProcessor.processTaxes(currYear, config)
        result.fed.shouldBe((wageInc.amount() - stdDeduct) * fedTaxRate)
        result.state.shouldBe((wageInc.amount() - stdDeduct) * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount() * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount() * medicareTaxCalc.pct)
    }

    should("processTaxes wage and other (no payroll tax) income no expenses") {
        val currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc, fedOnlyInc)
        )

        val result = TaxesProcessor.processTaxes(currYear, config)
        result.fed.shouldBe((wageInc.amount() + fedOnlyInc.amount() - stdDeduct) * fedTaxRate)
        result.state.shouldBe((wageInc.amount() - stdDeduct) * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount() * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount() * medicareTaxCalc.pct)
    }

    should("processTaxes wage and other (no payroll tax) income one deductible expense and one non-deductible") {
        val currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc, fedOnlyInc),
            expenses = listOf(nonDeductExp, decductExp)
        )

        val result = TaxesProcessor.processTaxes(currYear, config)
        result.fed.shouldBe((wageInc.amount() + fedOnlyInc.amount() - decductExp.amount() - stdDeduct) * fedTaxRate)
        result.state.shouldBe((wageInc.amount() - decductExp.amount() - stdDeduct) * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount() * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount() * medicareTaxCalc.pct)
    }

    should("processTaxes wage and other (no payroll tax) income one deductible expense and one non-deductible and asset gains") {
        val currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc, fedOnlyInc),
            expenses = listOf(nonDeductExp, decductExp),
            assets = listOf(assetRec)
        )

        val result = TaxesProcessor.processTaxes(currYear, config)
        result.fed.shouldBe((wageInc.amount() + fedOnlyInc.amount() + assetRec.totalGains() - decductExp.amount() - stdDeduct) * fedTaxRate)
        result.state.shouldBe((wageInc.amount() + assetRec.totalGains() - decductExp.amount() - stdDeduct) * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount() * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount() * medicareTaxCalc.pct)
    }
})


