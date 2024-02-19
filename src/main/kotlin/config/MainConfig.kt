package config

import Year
import asset.AssetConfigProgression
import expense.ExpenseConfigProgression
import inflation.InflationRec
import progression.Progression
import tax.TaxCalcConfig

data class MainConfig(
    val startYear: Year,
    val householdMembers: HouseholdMembers,
    val inflationConfig: Progression<InflationRec>,
    var householdExpenses: List<ExpenseConfigProgression> = ArrayList(),
    var jointAssets: List<AssetConfigProgression> = ArrayList(),
    var taxConfig: TaxCalcConfig,
)

/*
class EmploymentConfig(
    val name: String,
    val person: Name,
    val dtRange: DateRange,
    val incomeConfig: IncomeConfig,
    val medInsProgression: Progression<EmployerMedInsConfig>,
    val retirementConfig: ArrayList<RetireContribConfig>
)

class EmployerMedInsConfig(
    val available: Boolean,
    val primaryCost: Amount = 0.0,
    val spouseCost: Amount = 0.0,
    val childrenCost: Amount = 0.0,
    val priority: Int = 1,
)
*/

