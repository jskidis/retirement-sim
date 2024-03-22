package asset

import Amount
import Name
import tax.TaxableAmounts
import util.moneyFormat

data class AssetChange(
    val name: Name,
    val amount: Amount,
    val taxable: TaxableAmounts? = null,
    val unrealized: Amount = 0.0,
    val isCarryOver: Boolean = false,
    val isReqDist: Boolean = false,
) {
    override fun toString(): String {
        val amountStr = "$name:Amount=${moneyFormat.format(amount)}"
        val unrealStr = if (unrealized != 0.0) ", Unrealized=${moneyFormat.format(unrealized)}" else ""
        val taxableStr = if (taxable != null && taxable.hasAmounts()) ", Taxable=$taxable" else ""
        return "($amountStr$unrealStr$taxableStr)"
    }
}
