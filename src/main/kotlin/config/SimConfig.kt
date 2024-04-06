package config

import Year
import inflation.InflationRec
import netspend.NetSpendAllocationConfig
import progression.Progression
import tax.ITaxesProcessor
import tax.TaxCalcConfig
import tax.TaxesProcessor

data class SimConfig(
    val startYear: Year,
    val household: HouseholdConfig,
    val inflationConfig: Progression<InflationRec>,
    val taxConfig: TaxCalcConfig,
    val assetOrdering: NetSpendAllocationConfig,
    val rothConversion: RothConversionConfig? = null,
    val taxesProcessor: ITaxesProcessor = TaxesProcessor
)
