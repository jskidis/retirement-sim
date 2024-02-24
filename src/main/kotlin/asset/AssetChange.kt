package asset

import Amount
import Name
import moneyFormat
import tax.TaxableAmounts

interface AssetChange {
    val name: Name
    val isCarryOver: Boolean
    fun taxable(): TaxableAmounts?
    fun totalAmount(): Amount
    fun unrealized(): Amount
}

data class SimpleAssetChange (
    override val name: Name,
    val amount: Amount,
    val taxable: TaxableAmounts? = null,
    override val isCarryOver: Boolean = false
) : AssetChange {

    override fun totalAmount() = amount
    override fun taxable(): TaxableAmounts? = taxable
    override fun unrealized(): Amount = 0.0
    override fun toString(): String {
        return "($name=Amount=${moneyFormat.format(totalAmount())}" +
            if (taxable != null) ", Taxable=$taxable)" else ")"
    }
}

data class TaxableInvestGains(
    override val name: Name,
    val regTaxable: Amount,
    val ltTaxable: Amount,
    val unrealized: Amount,
    val taxable: TaxableAmounts? = null,
    override val isCarryOver: Boolean = false
) : AssetChange {

    override fun totalAmount(): Amount = regTaxable + ltTaxable + unrealized
    override fun taxable(): TaxableAmounts? = taxable
    override fun unrealized() = unrealized

    override fun toString(): String {
        val regTaxStr = if (regTaxable != 0.0) "RegTax=${moneyFormat.format(regTaxable)}, " else ""
        val ltgTaxStr = if (ltTaxable != 0.0) "LTGTax=${moneyFormat.format(ltTaxable)}, " else ""
        val unrealStr = if (unrealized != 0.0) "Unrealized=${moneyFormat.format(unrealized)}, " else ""
        val fullStr = "$regTaxStr$ltgTaxStr$unrealStr"
        val trimmedStr = if(fullStr.length < 2) fullStr else fullStr.dropLast(2)
        return "($name:(Total=${moneyFormat.format(totalAmount())}, $trimmedStr)"
    }
}