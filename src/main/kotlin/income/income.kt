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

interface IncomeRec : AmountRec {
    fun updateTaxable(taxable: TaxableAmounts): IncomeRec
}

data class StdIncomeRec(
    override val year: Year,
    override val ident: RecIdentifier,
    val amount: Amount,
    val taxableIncome: TaxableAmounts,
) : IncomeRec {
    override fun amount(): Amount = amount
    override fun taxable(): TaxableAmounts = taxableIncome
    override fun toString(): String = toJsonStr()

    override fun updateTaxable(taxable: TaxableAmounts): IncomeRec =
        this.copy(taxableIncome = taxable)
}

data class IncomeWithBonusRec(
    override val year: Year,
    override val ident: RecIdentifier,
    val baseAmount: Amount,
    val bonus: Amount = 0.0,
    val taxableIncome: TaxableAmounts,
) : IncomeRec {
    override fun amount(): Amount = baseAmount + bonus
    override fun taxable(): TaxableAmounts = taxableIncome
    override fun toString(): String = toJsonStr()

    override fun updateTaxable(taxable: TaxableAmounts): IncomeRec =
        this.copy(taxableIncome = taxable)
}

open class IncomeRecProvider(
    val ident: RecIdentifier,
    val taxabilityProfile: TaxabilityProfile
) : AmountToRecProvider<IncomeRec> {

    override fun createRecord(value: Amount, year: Year) = StdIncomeRec(
        year = year,
        ident = ident,
        amount = value,
        taxableIncome = taxabilityProfile.calcTaxable(ident.person, value))
}

typealias IncomeProgression = Progression<IncomeRec>
