package asset

import Amount
import Name
import tax.TaxableAmounts
import toJsonStr

data class AssetChange(
    val name: Name,
    val amount: Amount,
    val taxable: TaxableAmounts? = null,
    val unrealized: Amount = 0.0,
    val isCarryOver: Boolean = false,
    val isCashflowEvent: Boolean = false,
) {
    override fun toString(): String = toJsonStr()
}
