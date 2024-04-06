package income

import Amount
import AmountRec
import RecIdentifier
import Year
import config.AmountConfig
import config.SimpleAmountConfig
import progression.AmountToRecProvider
import tax.TaxabilityProfile
import tax.TaxableAmounts
import toJsonStr

data class IncomeRec(
    val year: Year,
    val ident: RecIdentifier,
    val baseAmount: Amount,
    val bonus: Amount = 0.0,
    val taxableIncome: TaxableAmounts,
) : AmountRec {
    // TODO: Remove me
    val config: AmountConfig = SimpleAmountConfig(ident.name, ident.person)

    override fun year(): Year = year
    override fun config(): AmountConfig = config
    override fun amount(): Amount = baseAmount + bonus

    override fun taxable(): TaxableAmounts = taxableIncome
    override fun retainRec(): Boolean = baseAmount != 0.0

    override fun toString(): String = toJsonStr()
}

/*
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
*/

open class IncomeRecProvider(
    val ident: RecIdentifier,
    val taxabilityProfile: TaxabilityProfile
)
    : AmountToRecProvider<IncomeRec> {

    override fun createRecord(value: Amount, year: Year) = IncomeRec(
        year = year,
        ident = ident,
        baseAmount = value,
        taxableIncome = taxabilityProfile.calcTaxable(ident.person, value))
}
