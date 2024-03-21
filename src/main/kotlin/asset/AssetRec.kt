package asset

import Amount
import AmountRec
import Year
import config.AmountConfig
import tax.TaxableAmounts
import util.PortionOfYearPast
import util.moneyFormat

data class AssetRec(
    val year: Year,
    val config: AssetConfig,
    val startBal: Amount,
    val startUnrealized: Amount,
    val gains: AssetChange,
) : AmountRec {
    val tributions: MutableList<AssetChange> = ArrayList()

    override fun year(): Year  = year
    override fun config(): AmountConfig = config
    override fun retainRec(): Boolean = startBal != 0.0 || finalBalance() != 0.0

    fun totalGains(): Amount = gains.amount
    fun capturedGains(): Amount = PortionOfYearPast.calc(year) * totalGains()
    fun totalTributions(): Amount = tributions.sumOf { it.amount }

    override fun taxable(): TaxableAmounts {
        return (tributions.map { it.taxable } + gains.taxable)
            .mapNotNull { it }
            .fold(TaxableAmounts(config.person)) { acc, it ->
                acc.plus(it)
            }
    }

    fun finalBalance(): Amount {
        val balance = startBal + totalGains() - capturedGains() + totalTributions()
        return if (balance < 100.0) 0.0 else balance
    }

    fun totalUnrealized(): Amount =
        startUnrealized + (tributions + gains).sumOf { it.unrealized }

    override fun toString(): String =
        "($config:(StartBal=${moneyFormat.format(startBal)}, " +
            "StartUnrealized=${moneyFormat.format(startUnrealized)}, " +
            "Gains=$gains, " +
            "TotalGains=${moneyFormat.format(totalGains())}, " +
            "CapturedGains=${moneyFormat.format(capturedGains())}, " +
            "Tributions=$tributions, " +
            "NetTributions=${moneyFormat.format(totalTributions())}, " +
            "FinalUnrealized=${moneyFormat.format(totalUnrealized())}, " +
            "FinalBal=${moneyFormat.format(finalBalance())})"
}
