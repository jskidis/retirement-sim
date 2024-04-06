package socsec

import Amount
import AmountRec
import RecIdentifier
import Year
import progression.Progression
import tax.TaxableAmounts
import toJsonStr

data class SSBenefitRec(
    val year: Year,
    val ident: RecIdentifier,
    val amount: Amount,
    val taxableAmount: TaxableAmounts,
) : AmountRec {

    override fun year(): Year = year
    override fun ident(): RecIdentifier = ident
    override fun amount(): Amount = amount
    override fun taxable(): TaxableAmounts = taxableAmount
    override fun retainRec(): Boolean = amount != 0.0

    override fun toString(): String = toJsonStr()
}

typealias SSBenefitProgression = Progression<SSBenefitRec>
