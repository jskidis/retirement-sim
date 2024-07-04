package expense

import Amount
import AmountRec
import RecIdentifier
import Year
import YearlyDetail
import progression.AmountToRecProvider
import progression.Progression
import tax.TaxabilityProfile
import tax.TaxableAmounts
import toJsonStr
import util.yearFromPrevYearDetail

data class ExpenseRec(
    override val year: Year,
    override val ident: RecIdentifier,
    val amount: Amount,
    val taxDeductions: TaxableAmounts,
): AmountRec {
    override fun amount(): Amount  = amount
    override fun taxable(): TaxableAmounts = taxDeductions
    override fun toString(): String = toJsonStr()
}

open class ExpenseRecProvider(
    val ident: RecIdentifier,
    val taxabilityProfile: TaxabilityProfile
) : AmountToRecProvider<ExpenseRec> {

    override fun createRecord(value: Amount, prevYear: YearlyDetail?) = ExpenseRec(
        year = yearFromPrevYearDetail(prevYear),
        ident = ident,
        amount = value,
        taxDeductions = taxabilityProfile.calcTaxable(ident.person, value)
    )
}

typealias ExpenseProgression = Progression<ExpenseRec>
