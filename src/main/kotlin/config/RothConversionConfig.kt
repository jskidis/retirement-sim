package config

import RecIdentifier
import asset.RothConversionAmountCalc
import tax.TaxabilityProfile
import util.YearBasedConfig

class RothConversionConfig(
    val amountCalc: YearBasedConfig<RothConversionAmountCalc>,
    val sourceDestPairs: List<Pair<RecIdentifier, RecIdentifier>>,
    val taxabilityProfile: TaxabilityProfile,
)