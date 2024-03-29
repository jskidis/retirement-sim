package tax

import Year
import asset.AssetChange
import asset.assetConfigProgressFixture
import asset.assetRecFixture
import config.configFixture
import expense.expenseRecFixture
import income.incomeRecFixture
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
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

    val noCarryOver = ArrayList<TaxableAmounts>()
    val carryOver = TaxableAmounts(person = "P", fed = 1000.0, fedLTG = 100.0, state = 1100.0)

    val fedTaxCalc = FixedRateTaxCalc(.10)
    val fedLTGTaxCalc = FixedRateTaxCalc(.05)
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

        val result = TaxesProcessor.processTaxes(currYear, noCarryOver, config)
        result.fed.shouldBe((wageInc.amount() - stdDeduct) * fedTaxCalc.pct)
        result.state.shouldBe((wageInc.amount() - stdDeduct) * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount() * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount() * medicareTaxCalc.pct)
    }

    should("processTaxes wage and other (no payroll tax) income no expenses") {
        val currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc, fedOnlyInc)
        )

        val result = TaxesProcessor.processTaxes(currYear, noCarryOver, config)
        result.fed.shouldBe((wageInc.amount() + fedOnlyInc.amount() - stdDeduct) * fedTaxCalc.pct)
        result.state.shouldBe((wageInc.amount() - stdDeduct) * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount() * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount() * medicareTaxCalc.pct)
    }

    should("processTaxes wage and other (no payroll tax) income one deductible expense and one non-deductible") {
        val currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc, fedOnlyInc),
            expenses = listOf(nonDeductExp, decductExp)
        )

        val result = TaxesProcessor.processTaxes(currYear, noCarryOver, config)
        result.fed.shouldBe((wageInc.amount() + fedOnlyInc.amount() - decductExp.amount() - stdDeduct) * fedTaxCalc.pct)
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

        val result = TaxesProcessor.processTaxes(currYear, noCarryOver, config)
        result.fed.shouldBe((wageInc.amount() + fedOnlyInc.amount() + assetRec.totalGains() - decductExp.amount() - stdDeduct) * fedTaxCalc.pct)
        result.state.shouldBe((wageInc.amount() + assetRec.totalGains() - decductExp.amount() - stdDeduct) * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount() * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount() * medicareTaxCalc.pct)
    }

    should("processTaxes includes carryOver taxable in calcuations") {
        val currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc)
        )

        val result = TaxesProcessor.processTaxes(currYear, listOf(carryOver), config)
        result.fed.shouldBe((
            wageInc.amount() + carryOver.fed - stdDeduct) * fedTaxCalc.pct +
            (carryOver.fedLTG * fedLTGTaxCalc.pct))
        result.state.shouldBe((wageInc.amount() + carryOver.state - stdDeduct) * stateTaxCalc.pct)
        result.socSec.shouldBe(wageInc.amount() * socSecTaxCalc.pct)
        result.medicare.shouldBe(wageInc.amount() * medicareTaxCalc.pct)

    }

    should("calculate carryOver taxable amounts") {
        val year: Year = 2020

        val assetConfig1 = assetConfigProgressFixture(name = "Asset 1")
        val taxable1 = TaxableAmounts(person = "Person", fed = 1000.0, state = 1000.0)
        val assetRec1 = assetRecFixture(year = year, assetConfig = assetConfig1.config)
        val tribution1 = AssetChange(name = "W", amount = -1000.0, taxable1, isCarryOver = true)
        assetRec1.tributions.add(tribution1)

        val assetConfig2 = assetConfigProgressFixture(name = "Asset 2")
        val taxable2 = TaxableAmounts(person = "Person", fed = 2000.0, state = 2000.0)
        val assetRec2 = assetRecFixture(year = year, assetConfig = assetConfig2.config)
        val tribution2 = AssetChange(name = "W", amount = -2000.0, taxable2, isCarryOver = true)
        assetRec2.tributions.add(tribution2)

        val assetConfig3 = assetConfigProgressFixture(name = "Asset 3")
        val taxable3 = TaxableAmounts(person = "Person", fed = -3000.0, state = -3000.0)
        val assetRec3 = assetRecFixture(year = year, assetConfig = assetConfig3.config)
        val tribution3 = AssetChange(name = "D", amount = 3000.0, taxable3, isCarryOver = false)
        assetRec3.tributions.add(tribution3)

        val assetConfig4 = assetConfigProgressFixture(name = "Asset 4")
        val assetRec4 = assetRecFixture(year = year, assetConfig = assetConfig4.config)
        val tribution4 = AssetChange(name = "D", amount = 100.0, taxable = null, isCarryOver = true)
        assetRec4.tributions.add(tribution4)

        val currYear = yearlyDetailFixture(year, assets = listOf(
            assetRec1, assetRec2, assetRec3, assetRec4
        ))

        val result = TaxesProcessor.carryOverTaxable(currYear)
        result.shouldHaveSize(2)
        result.shouldContain(taxable1)
        result.shouldContain(taxable2)
    }

    should("Determine carry over penalty, taxes include curr year carryover minus taxes without them") {
        val inflationRate = .04
        var currYear = yearlyDetailFixture().copy(
            incomes = listOf(wageInc),
            inflation = inflationRecFixture(stdRAC = InflationRAC(inflationRate))
        )

        val assetConfig1 = assetConfigProgressFixture(name = "Asset 1")
        val taxable1 = TaxableAmounts(person = "Person", fed = 1000.0, state = 1000.0)
        val assetRec1 = assetRecFixture(year = currYear.year, assetConfig = assetConfig1.config)
        val tribution1 = AssetChange(name = "W", amount = -1000.0, taxable1, isCarryOver = true)
        assetRec1.tributions.add(tribution1)

        val taxesWithoutCO = TaxesProcessor.processTaxes(currYear, ArrayList(), config )
        currYear = currYear.copy(taxes = taxesWithoutCO, assets = listOf(assetRec1))

        val carryOverTaxable = TaxesProcessor.carryOverTaxable(currYear)
        currYear = currYear.copy(carryOverTaxable = carryOverTaxable)

        val taxesWithCO = TaxesProcessor.processTaxes(currYear, carryOverTaxable, config)
        val expectedPenalty = 2 * inflationRate *
            (taxesWithCO.total() - taxesWithoutCO.total())

        TaxesProcessor.carryOverPenalty(currYear, config)
            .shouldBeWithinPercentageOf(expectedPenalty, .001)
    }
})


