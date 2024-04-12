package socsec

import Amount
import AmountRec
import Rate
import RecIdentifier
import Year
import progression.Progression
import tax.TaxableAmounts
import toJsonStr

data class SSBenefitRec(
    override val year: Year,
    override val ident: RecIdentifier,
    val amount: Amount,
    val taxableAmount: TaxableAmounts,
    val baseAmount: Amount = 0.0,
    val benefitAdjustment: Rate = 0.0,
) : AmountRec {

    override fun amount(): Amount = amount
    override fun taxable(): TaxableAmounts = taxableAmount
    override fun retainRec(): Boolean = true
    override fun toString(): String = toJsonStr()

    fun hasClaimed(): Boolean = benefitAdjustment != 0.0
}

interface SSBenefitProgression: Progression<SSBenefitRec> {
    fun isPrimary(): Boolean
}

