package asset

import Amount
import AmountRec
import RecIdentifier
import Year
import tax.TaxableAmounts
import toJsonStr
import util.PortionOfYearPast

data class AssetRec(
    override val year: Year,
    override val ident: RecIdentifier,
    val assetType: AssetType,
    val startBal: Amount,
    val startUnrealized: Amount,
    val gains: AssetChange,
) : AmountRec {
    val tributions: MutableList<AssetChange> = ArrayList()

    override fun toString(): String = toJsonStr()

    override fun amount(): Amount = finalBalance()
    override fun retainRec(): Boolean = startBal != 0.0 || finalBalance() != 0.0

    override fun taxable(): TaxableAmounts {
        return (tributions.map { it.taxable } + gains.taxable)
            .mapNotNull { it }
            .fold(TaxableAmounts(ident.person)) { acc, it ->
                acc.plus(it)
            }
    }

    fun totalGains(): Amount = gains.amount
    fun proratedGains(): Amount = PortionOfYearPast.calc(year) * totalGains()
    fun nonAccruedTributions(): Amount = tributions.sumOf { it.amount - it.accruedAmt }
    fun totalTributions(): Amount = tributions.sumOf { it.amount }


    fun finalBalance(): Amount {
        val balance = startBal + totalGains() - proratedGains() + nonAccruedTributions()
        return if (balance < 100.0) 0.0 else balance
    }

    fun totalUnrealized(): Amount =
        startUnrealized + (tributions + gains).sumOf { it.unrealized }
}
