package config

import Year
import inflation.InflationRec
import progression.Progression
import tax.TaxCalcConfig

data class SimConfig(
    val startYear: Year,
    val household: HouseholdConfig,
    val inflationConfig: Progression<InflationRec>,
    var taxConfig: TaxCalcConfig,
)

/*
class EmploymentConfig(
    val name: String,
    val person: Name,
    val dtRange: util.DateRange,
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

