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

interface IEmployerInsurance {
    val selfCost: Amount
    val spouseCost: Amount
    val dependentCost: Amount
}

data class EmployerInsurance(
    override val selfCost: Amount,
    override val spouseCost: Amount,
    override val dependentCost: Amount,
    val cobraConfig: CobraConfig? = null,
) : IEmployerInsurance

data class CobraConfig(
    override val selfCost: Amount,
    override val spouseCost: Amount,
    override val dependentCost: Amount,
    val months: Int = 18,
) : IEmployerInsurance {

    fun effectiveDates(empConfig: EmploymentConfig) = DateRange(
        start = empConfig.dateRange.end,
        end = empConfig.dateRange.end.plusMonths(months)
    )
}

