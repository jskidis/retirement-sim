package tax

import Amount
import Rate
import YearlyDetail

class taxCalculatorFixture : TaxCalculator {
    override fun determineTax(taxableAmount: Amount, currYear: YearlyDetail): Amount = 0.0
    override fun marginalRate(taxableAmount: Amount, currYear: YearlyDetail): Rate = 0.0
}

fun taxConfigFixture() = TaxCalcConfig(
    fed = taxCalculatorFixture(),
    fedLTG = taxCalculatorFixture(),
    state = taxCalculatorFixture(),
    socSec = taxCalculatorFixture(),
    medicare = taxCalculatorFixture(),
)

open class TaxabilityProfileFixture : TaxabilityProfile {
    override fun fed(amount: Amount): Amount = 0.0
    override fun state(amount: Amount): Amount = 0.0
    override fun socSec(amount: Amount): Amount = 0.0
    override fun medicare(amount: Amount): Amount = 0.0
}
