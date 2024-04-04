package income

import Amount
import AmountRec
import Name
import Year
import config.AmountConfig
import progression.AmountToRecProvider
import progression.Progression
import tax.TaxabilityProfile
import tax.TaxableAmounts
import tax.UnusedProfile
import toJsonStr

data class IncomeRec(
    val year: Year,
    val config: IncomeConfig,
    val baseAmount: Amount,
    val bonus: Amount = 0.0,
    val taxableIncome: TaxableAmounts,
) : AmountRec {

    override fun year(): Year = year
    override fun config(): AmountConfig = config
    override fun amount(): Amount = baseAmount + bonus

    override fun taxable(): TaxableAmounts = taxableIncome
    override fun retainRec(): Boolean = baseAmount != 0.0

    override fun toString(): String = toJsonStr()
}

data class IncomeConfig(
    override val name: Name,
    override val person: Name,
    override val taxabilityProfile: TaxabilityProfile = UnusedProfile(),
) : AmountConfig {
    override fun toString(): String = toJsonStr()
}

data class IncomeConfigProgression(
    val config: IncomeConfig,
    val progression: Progression<IncomeRec>,
)

open class IncomeRecProvider(val config: IncomeConfig)
    : AmountToRecProvider<IncomeRec> {

    override fun createRecord(value: Amount, year: Year) = IncomeRec(
        year = year,
        config = config,
        baseAmount = value,
        taxableIncome = config.taxabilityProfile.calcTaxable(config.person, value))
}
