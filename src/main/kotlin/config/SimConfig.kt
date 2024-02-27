package config

import Year
import inflation.InflationRec
import progression.Progression
import tax.TaxCalcConfig

data class SimConfig(
    val startYear: Year,
    val household: HouseholdConfig,
    val inflationConfig: Progression<InflationRec>,
    val taxConfig: TaxCalcConfig,
)
