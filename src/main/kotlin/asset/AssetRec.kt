package asset

import Amount
import AmountRec
import RecIdentifier
import Year
import tax.TaxableAmounts
import toJsonStr
import util.PortionOfYearPast

data class AssetRec(
    val year: Year,
    val ident: RecIdentifier,
    val startBal: Amount,
    val startUnrealized: Amount,
    val gains: AssetChange,
) : AmountRec {
    val tributions: MutableList<AssetChange> = ArrayList()

    override fun toString(): String = toJsonStr()

    override fun year(): Year = year
    override fun ident(): RecIdentifier = ident
    override fun amount(): Amount = finalBalance()
    override fun retainRec(): Boolean = startBal != 0.0 || finalBalance() != 0.0

    fun totalGains(): Amount = gains.amount
    fun capturedGains(): Amount = PortionOfYearPast.calc(year) * totalGains()
    fun totalTributions(): Amount = tributions.sumOf { it.amount }

    override fun taxable(): TaxableAmounts {
        return (tributions.map { it.taxable } + gains.taxable)
            .mapNotNull { it }
            .fold(TaxableAmounts(ident.person)) { acc, it ->
                acc.plus(it)
            }
    }

    fun finalBalance(): Amount {
        val balance = startBal + totalGains() - capturedGains() + totalTributions()
        return if (balance < 100.0) 0.0 else balance
    }

    fun totalUnrealized(): Amount =
        startUnrealized + (tributions + gains).sumOf { it.unrealized }
}
