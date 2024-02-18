package config

import Amount
import AssetRec
import DateRange
import Name
import Year
import expense.ExpenseConfigProgression
import income.IncomeConfig
import inflation.InflationRec
import progression.Progression
import tax.TaxCalculator

data class MainConfig(
    val startYear: Year,
    val householdMembers: HouseholdMembers,
    val inflationConfig: Progression<InflationRec>,
    val householdExpenses: ArrayList<ExpenseConfigProgression> = ArrayList(),
    val jointAssets: ArrayList<AssetConfig> = ArrayList(),
    val taxConfig: TaxCalcConfig
)

data class TaxCalcConfig(
    val fed: TaxCalculator,
    val state: TaxCalculator,
    val socSec: TaxCalculator,
    val medicare: TaxCalculator
)

class EmploymentConfig(
    val name: String,
    val person: Name,
    val dtRange: DateRange,
    val incomeConfig: IncomeConfig,
    val medInsProgression: Progression<EmployerMedInsConfig>,
    val retirementConfig: ArrayList<RetireContribConfig>
)

class AssetConfig(
    val name: String,
    val person: Name,
    val progression: Progression<AssetRec>
)

class EmployerMedInsConfig(
    val available: Boolean,
    val primaryCost: Amount = 0.0,
    val spouseCost: Amount = 0.0,
    val childrenCost: Amount = 0.0,
    val priority: Int = 1,
)

class RetireContribConfig(
)

