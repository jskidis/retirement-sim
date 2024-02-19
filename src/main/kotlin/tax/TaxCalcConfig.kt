package tax

data class TaxCalcConfig(
    val fed: TaxCalculator,
    val state: TaxCalculator,
    val socSec: TaxCalculator,
    val medicare: TaxCalculator
)