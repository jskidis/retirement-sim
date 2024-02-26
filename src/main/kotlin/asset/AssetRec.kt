package asset

import Amount
import Year
import moneyFormat
import tax.TaxableAmounts
import util.PortionOfYearPast

data class AssetRec(
    val year: Year,
    val config: AssetConfig,
    val startBal: Amount,
    val gains: AssetChange,
) {
    val tributions: MutableList<AssetChange> = ArrayList()

    fun totalGains(): Amount = gains.totalAmount()
    fun capturedGains(): Amount = PortionOfYearPast.calc(year) * totalGains()
    fun totalTributions(): Amount = tributions.sumOf { it.totalAmount() }
    fun taxable(): TaxableAmounts {
        return (tributions.map { it.taxable() } + gains.taxable())
            .mapNotNull { it }
            .fold(TaxableAmounts(config.person)) { acc, it ->
                acc.plus(it)
            }
    }
    fun finalBalance(): Amount {
        val balance = startBal + totalGains() - capturedGains() + totalTributions()
        return if (balance < 100.0) 0.0 else balance
    }

    override fun toString(): String =
        "($config:(startBal=${moneyFormat.format(startBal)}, " +
            "Gains=$gains, " +
            "TotalGains=${moneyFormat.format(totalGains())}, " +
            "CapturedGains=${moneyFormat.format(capturedGains())}, " +
            "Tributions=$tributions, " +
            "Tributions=${moneyFormat.format(totalTributions())}, " +
            "FinalBal=${moneyFormat.format(finalBalance())})"
}
