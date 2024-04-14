package socsec

import Amount
import AmountRec
import Rate
import RecIdentifier
import Year
import YearMonth
import YearlyDetail
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
    val claimDate: YearMonth? = null
) : AmountRec {

    override fun amount(): Amount = amount
    override fun taxable(): TaxableAmounts = taxableAmount
    override fun retainRec(): Boolean = baseAmount != 0.0
    override fun toString(): String = toJsonStr()
}

interface SSBenefitProgression: Progression<SSBenefitRec>

interface SecondarySSBenefitProgression {
    fun determineNext(prevYear: YearlyDetail?, currYear: YearlyDetail): SSBenefitRec
}

