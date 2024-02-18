package tax

import Amount
import Rate
import YearlyDetail
import config.MainConfig

class EmployeeSocSecTaxCalc : FixedRateTaxCalc(0.062)
class EmployeeMedicareTaxCalc : FixedRateTaxCalc(0.0145)
class ContractorSocSecTaxCalc : FixedRateTaxCalc(0.124)
class ContractorMedicareTaxCalc : FixedRateTaxCalc(0.0290)

open class FixedRateTaxCalc(val pct: Rate) : TaxCalculator {
    override fun determineTax(
        taxableAmount: Amount,
        config: MainConfig, currYear: YearlyDetail,
    ): Amount = taxableAmount * pct
}
