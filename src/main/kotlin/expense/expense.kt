package expense

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

data class ExpenseRec(
    val year: Year,
    val ident: RecIdentifier,
    val amount: Amount,
    val taxDeductions: TaxableAmounts,
): AmountRec {
    // TODO: Remove me
    val config: AmountConfig = SimpleAmountConfig(ident.name, ident.person)

    override fun year(): Year  = year
    override fun config(): AmountConfig = config
    override fun amount(): Amount  = amount
    override fun taxable(): TaxableAmounts = taxDeductions
    override fun retainRec(): Boolean = amount != 0.0

    override fun toString(): String = toJsonStr()
}

open class ExpenseRecProvider(
    val ident: RecIdentifier,
    val taxabilityProfile: TaxabilityProfile
) : AmountToRecProvider<ExpenseRec> {

    override fun createRecord(value: Amount, year: Year) = ExpenseRec(
        year = year,
        ident = ident,
        amount = value,
        taxDeductions = taxabilityProfile.calcTaxable(ident.person, value)
    )
}


