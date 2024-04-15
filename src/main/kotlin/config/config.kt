package config

import inflation.InflationRec
import progression.Progression
import tax.TaxCalcConfig
import util.YearBasedConfig

typealias InflationConfig = Progression<InflationRec>
typealias TaxCalcYearlyConfig = YearBasedConfig<TaxCalcConfig>
