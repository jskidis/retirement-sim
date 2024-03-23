package asset

import Amount
import AmountRec
import Year
import config.AmountConfig
import income.IncomeConfig
import income.IncomeRec
import tax.TaxableAmounts
import util.PortionOfYearPast
import util.moneyFormat
import util.strWhenNotZero

data class AssetRec(
    val year: Year,
    val config: AssetConfig,
    val startBal: Amount,
    val startUnrealized: Amount,
    val gains: AssetChange,
) : AmountRec {
    val tributions: MutableList<AssetChange> = ArrayList()

    override fun year(): Year = year
    override fun config(): AmountConfig = config
    override fun retainRec(): Boolean = startBal != 0.0 || finalBalance() != 0.0

    fun totalGains(): Amount = gains.amount
    fun capturedGains(): Amount = PortionOfYearPast.calc(year) * totalGains()
    fun totalTributions(): Amount = tributions.sumOf { it.amount }

    override fun taxable(): TaxableAmounts {
        return (tributions.filter { !it.isReqDist }.map { it.taxable } + gains.taxable)
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

    fun incomeRecs(): List<IncomeRec> =
        tributions.filter { it.isReqDist }.map {
            IncomeRec(
                year = year,
                config = IncomeConfig(
                    name = it.name,
                    person = config.person,
                    config.taxabilityProfile
                ),
                amount = -it.amount,
                taxableIncome = it.taxable ?: TaxableAmounts(config.person)
            )
        }

    override fun toString(): String {
        return "($config:(StartBal=${moneyFormat.format(startBal)}, " +
            strWhenNotZero(
                startUnrealized == 0.0,
                "StartUnrealized=${moneyFormat.format(startUnrealized)}, "
            ) +
            strWhenNotZero(
                totalGains() == 0.0,
                "Gains=$gains, TotalGains=${moneyFormat.format(totalGains())}, " +
                    strWhenNotZero(
                        capturedGains() == 0.0,
                        "CapturedGains=${moneyFormat.format(capturedGains())}, ")
            ) +
            strWhenNotZero(
                tributions.isEmpty(),
            "Tributions=$tributions, NetTributions=${moneyFormat.format(totalTributions())}, "
            ) +
            strWhenNotZero(
                totalUnrealized() == 0.0,
                "FinalUnrealized=${moneyFormat.format(totalUnrealized())}, "
            ) +
            "FinalBal=${moneyFormat.format(finalBalance())})"
    }

    /*
        override fun toString(): String =
            "($config:(StartBal=${moneyFormat.format(startBal)})" +
    //            "FinalBal=${moneyFormat.format(finalBalance())})" +
        ""
    */
}
