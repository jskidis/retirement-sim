package asset

import Amount
import Name
import tax.TaxableAmounts
import util.moneyFormat
import util.strWhenNotZero

data class AssetChange(
    val name: Name,
    val amount: Amount,
    val taxable: TaxableAmounts? = null,
    val unrealized: Amount = 0.0,
    val isCarryOver: Boolean = false,
    val isReqDist: Boolean = false,
) {
    override fun toString(): String = "{" +
        "\"name\":\"$name\"" +
        ", \"amount\":\"${moneyFormat.format(amount)}\"" +
        strWhenNotZero(unrealized == 0.0, ", \"unrealized\":\"${moneyFormat.format(unrealized)}\"") +
        strWhenNotZero(taxable == null, ", \"taxable\":$taxable") +
        "}"
}
