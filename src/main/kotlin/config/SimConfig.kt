package config

import Year
import YearlyDetail
import asset.AssetProgression
import expense.ExpenseProgression
import income.IncomeProgression
import inflation.InflationRec
import netspend.NetSpendAllocationConfig
import progression.Progression
import tax.ITaxesProcessor
import tax.TaxCalcConfig
import tax.TaxesProcessor
import util.YearBasedConfig

data class SimConfig(
    val startYear: Year,
    val household: HouseholdConfig,
    val inflationConfig: Progression<InflationRec>,
    val taxConfig: YearBasedConfig<TaxCalcConfig>,
    val assetOrdering: NetSpendAllocationConfig,
    val rothConversion: RothConversionConfig? = null,
    val taxesProcessor: ITaxesProcessor = TaxesProcessor,
) {
    fun incomeConfigs(): List<IncomeProgression> =
        household.members.people().flatMap { it.incomes() }

    fun expenseConfigs(): List<ExpenseProgression>  =
        household.expenses +
            household.members.people().flatMap { it.expenses() }

    fun assetConfigs(): List<AssetProgression> =
        household.jointAssets +
            household.members.people().flatMap { it.assets() }

    fun currTaxConfig(currYear: YearlyDetail): TaxCalcConfig =
        taxConfig.getConfigForYear(currYear.year)
}
