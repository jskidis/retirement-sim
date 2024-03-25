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

