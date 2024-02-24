package asset

import Amount
import YearlyDetail
import moneyFormat
import tax.TaxableAmounts
import util.PortionOfYearPast

data class AssetRec(
    val config: AssetConfig,
    val startBal: Amount,
    val gains: AssetChange,
    val unrealizedGains: Amount = 0.0
) {
    val tributions: MutableList<AssetChange> = ArrayList()
    var calcValues: AssetCalcValuesRec = AssetCalcValuesRec()

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
            val finalBal = assetRec.startBal + totalGains - capturedGains + totalTributions
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