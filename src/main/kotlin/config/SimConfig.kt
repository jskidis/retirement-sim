package config

import Year
import YearlyDetail
import YearlySummary
import asset.AssetProgression
import cashflow.CashFlowEventConfig
import expense.ExpenseProgression
import income.IncomeProgression
import netspend.NetSpendAllocationConfig
import tax.TaxCalcConfig
import tax.TaxProcessorConfig
import tax.TaxesProcessor

data class SimConfig(
    val startYear: Year,
    val household: HouseholdConfig,
    val inflationConfig: InflationConfig,
    val assetOrdering: NetSpendAllocationConfig,
    val taxCalcConfig: TaxCalcYearlyConfig,
    val taxesProcessor: TaxProcessorConfig = TaxesProcessor,
    val rothConversion: RothConversionConfig? = null,
    val simSuccess: SimSuccess = BasicSimSuccess(),
) {
    fun incomeConfigs(): List<IncomeProgression> =
        household.members.flatMap { it.incomes() }

    fun expenseConfigs(): List<ExpenseProgression> =
        household.expenses +
            household.members.flatMap { it.expenses() }

    fun assetConfigs(): List<AssetProgression> =
        household.jointAssets +
            household.members.flatMap { it.assets() }

    fun cashFlowConfigs(): List<CashFlowEventConfig> =
        household.members.flatMap { it.cashFlowEvents() }

    fun currTaxConfig(currYear: YearlyDetail): TaxCalcConfig =
        taxCalcConfig.getConfigForYear(currYear.year)
}

fun interface SimSuccess {
    fun wasSuccessRun(yearlySummary: YearlySummary): Boolean
}

class BasicSimSuccess : SimSuccess {
    override fun wasSuccessRun(yearlySummary: YearlySummary): Boolean =
        yearlySummary.assetValue > 0.0
}