package config

import Amount
import Name
import income.BonusCalculator
import income.IncomeConfig
import tax.WageTaxableProfile
import util.DateRange

data class EmploymentConfig(
    val name: Name,
    val person: Name,
    val dateRange: DateRange,
    val startSalary: Amount,
    val bonusCalc: BonusCalculator? = null,
) {
    companion object {
        fun incomeConfig(config: EmploymentConfig): IncomeConfig =
            IncomeConfig(
                name = config.name, person = config.person,
                taxabilityProfile = WageTaxableProfile()
            )
    }
}
/*
data class EmployerInsurance(
    val selfCost: Amount,
    val spouseCost: Amount,
    val dependantCost: Amount,
    val isSingleCostAllDependants: Boolean = false
)
*/