package asset

import Amount
import AssetGain
import AssetNetContribution
import YearlyDetail
import moneyFormat
import tax.TaxableAmounts
import util.PortionOfYearPast

data class AssetRec(
    val config: AssetConfig,
    val startBal: Amount,
    val taxable: TaxableAmounts,
    val gains: List<AssetGain> = ArrayList(),
    val contributions: List<AssetNetContribution> = ArrayList(),
    var calcValues: AssetCalcValuesRec = AssetCalcValuesRec()
) {
    override fun toString(): String =
        "($config:(startBal=${moneyFormat.format(startBal)}, " +
        "gains=$gains, " + "contributions=$contributions, " +
        "${calcValues}, taxableGains=$taxable)"
}

data class AssetCalcValuesRec(
    val totalGains: Amount = 0.0,
    val capturedGains: Amount = 0.0,
    val totalContributions: Amount = 0.0,
    val finalBal: Amount = 0.0
) {
    companion object {
        fun create(assetRec: AssetRec, currYear: YearlyDetail): AssetCalcValuesRec {
            val totalGains = assetRec.gains.sumOf { it.amount }
            val capturedGains = PortionOfYearPast.calc(currYear.year) * totalGains
            val totalContributions = assetRec.contributions.sumOf { it.amount }
            val finalBal = assetRec.startBal + totalGains - capturedGains + totalContributions
            return AssetCalcValuesRec(totalGains, capturedGains, totalContributions, finalBal)
        }
    }

    override fun toString(): String =
        "totalGains=${moneyFormat.format(totalGains)}, " +
            "capturedGains=${moneyFormat.format(capturedGains)}, " +
            "contributions=${moneyFormat.format(totalContributions)}, " +
            "finalBal=${moneyFormat.format(finalBal)}"
}