package config

import Amount
import RecIdentifier
import income.BonusCalculator
import tax.TaxabilityProfile
import tax.WageTaxableProfile
import util.DateRange

data class EmploymentConfig(
    val ident: RecIdentifier,
    val dateRange: DateRange,
    val startSalary: Amount,
    val bonusCalc: BonusCalculator? = null,
    val taxabilityProfile: TaxabilityProfile = WageTaxableProfile(),
    val employerInsurance: EmployerInsurance? = null,
)

data class EmployerInsurance(
    val selfCost: Amount,
    val spouseCost: Amount,
    val dependantCost: Amount,
    val cobraLength: Int = 18,
    val cobraMult: Double = 2.04
)