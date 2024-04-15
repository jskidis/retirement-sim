package config

import Year
import netspend.NetSpendAllocationConfig
import tax.TaxesProcessor
import util.currentDate

interface ConfigBuilder {
    fun startYear(): Year = currentDate.year
    fun householdConfig(): HouseholdConfig
    fun inflationConfig(): InflationConfig
    fun assetOrdering(): NetSpendAllocationConfig
    fun taxCalcConfig(): TaxCalcYearlyConfig
    fun taxesProcessor(): TaxesProcessor = TaxesProcessor
    fun rothConversionConfig(): RothConversionConfig? = null
    fun simSuccess(): SimSuccess = BasicSimSuccess()

    fun buildConfig(): SimConfig = SimConfig(
        startYear = startYear(),
        household = householdConfig(),
        inflationConfig = inflationConfig(),
        assetOrdering = assetOrdering(),
        taxCalcConfig = taxCalcConfig(),
        taxesProcessor = taxesProcessor(),
        rothConversion = rothConversionConfig(),
        simSuccess = simSuccess()
    )
}