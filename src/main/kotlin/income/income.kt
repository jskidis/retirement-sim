package income

import Amount
import AmountRec
import RecIdentifier
import Year
import progression.AmountToRecProvider
import progression.Progression
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
    override fun year(): Year = year
    override fun ident(): RecIdentifier = ident
    override fun amount(): Amount = baseAmount + bonus

    override fun taxable(): TaxableAmounts = taxableIncome
    override fun retainRec(): Boolean = baseAmount != 0.0

    override fun toString(): String = toJsonStr()
}

open class IncomeRecProvider(
    val ident: RecIdentifier,
    val taxabilityProfile: TaxabilityProfile
) : AmountToRecProvider<IncomeRec> {

    override fun createRecord(value: Amount, year: Year) = IncomeRec(
        year = year,
        ident = ident,
        baseAmount = value,
        taxableIncome = taxabilityProfile.calcTaxable(ident.person, value))
}

typealias IncomeProgression = Progression<IncomeRec>
