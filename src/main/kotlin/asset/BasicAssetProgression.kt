package asset

import Amount
import AssetGain
import AssetNetContribution
import YearlyDetail
import currentDate
import progression.Progression
import tax.TaxableAmounts

open class BasicAssetProgression(
    val startBalance: Amount,
    val config: AssetConfig,
) : Progression<AssetRec> {

    override fun determineNext(prevYear: YearlyDetail?): AssetRec {
        val composition = config.determineComposition(
            if (prevYear == null) currentDate.year
            else prevYear.year + 1
        )
        val balance =
            if (prevYear == null) startBalance
            else previousRec(prevYear)?.calcValues?.finalBal ?: startBalance

        val gains = calcGains(balance, composition, prevYear)
        val taxableAmounts = calcTaxable(gains)

        return AssetRec(
            config = config,
            startBal = balance,
            gains = gains,
            taxable = taxableAmounts
        )
    }

    private fun previousRec(prevYear: YearlyDetail): AssetRec? =
        prevYear.assets.find {
            it.config.person == config.person && it.config.name == config.name
        }

    private fun calcGains(
        balance: Amount,
        composition: List<AssetComposition>,
        prevYear: YearlyDetail?,
    ): List<AssetGain> {

        return composition.map {
            AssetNetContribution(
                name = it.name,
                amount = it.rorProvider.determineRate(prevYear) * it.pct * balance
            )
        }
    }

    private fun calcTaxable(gains: List<AssetGain>): TaxableAmounts =
        config.taxabilityProfile.calcTaxable(
            person = config.person,
            amount = gains.sumOf { it.amount })
}