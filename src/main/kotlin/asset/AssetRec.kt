package asset

import Amount
import Year
import YearlyDetail
import moneyFormat
import tax.TaxableAmounts
import util.PortionOfYearPast

data class AssetRec(
    val config: AssetConfig,
    val startBal: Amount,
    val gains: AssetChange
) {
    val tributions: MutableList<AssetChange> = ArrayList()
    var calcValues: AssetCalcValuesRec = AssetCalcValuesRec()

    fun totalGains(): Amount = gains.totalAmount()
    fun capturedGains(year: Year): Amount = PortionOfYearPast.calc(year) * totalGains()
    fun totalTributions(): Amount = tributions.sumOf { it.totalAmount() }
    fun finalBalance(year: Year): Amount {
        val balance = startBal + totalGains() - capturedGains(year) + totalTributions()
        return if (balance < 100.0) 0.0 else balance
    }

    override fun toString(): String =
        "($config:(startBal=${moneyFormat.format(startBal)}, " +
            "Gains=$gains, Tributions=$tributions, ${calcValues})"
}

data class AssetCalcValuesRec(
    val totalGains: Amount = 0.0,
    val capturedGains: Amount = 0.0,
    val totalTributions: Amount = 0.0,
    val finalBal: Amount = 0.0,
    val taxable: TaxableAmounts = TaxableAmounts(""),
) {
    companion object {
        fun create(assetRec: AssetRec, currYear: YearlyDetail): AssetCalcValuesRec {
            val totalGains = assetRec.gains.totalAmount()
            val capturedGains = PortionOfYearPast.calc(currYear.year) * totalGains
            val totalTributions = assetRec.tributions.sumOf { it.totalAmount() }
            val balance = assetRec.startBal + totalGains - capturedGains + totalTributions
            val finalBal = if (balance < 100.0) 0.0 else balance
            return AssetCalcValuesRec(
                totalGains, capturedGains, totalTributions,
                finalBal, determineTaxable(assetRec))
        }

        fun determineTaxable(assetRec: AssetRec): TaxableAmounts {
            return (assetRec.tributions.map { it.taxable() } + assetRec.gains.taxable())
                .mapNotNull { it }
                .fold(TaxableAmounts(assetRec.config.person)) { acc, it ->
                    acc.plus(it)
                }
        }
    }

    override fun toString(): String =

        "TotalGains=${moneyFormat.format(totalGains)}, " +
            "CapturedGains=${moneyFormat.format(capturedGains)}, " +
            "Tributions=${moneyFormat.format(totalTributions)}, " +
            "FinalBal=${moneyFormat.format(finalBal)}"
}