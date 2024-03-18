package asset

import Amount
import YearlyDetail
import tax.TaxableAmounts

open class TaxableInvestGainCreator(
    val qualDivRatio: Double = 0.8,
    val regTaxOnGainsPct: Double = 0.1,
    val ltTaxOnGainsPct: Double = 0.1,
) : AssetGainCreator, GrossGainsCalc {
    override fun createGain(
        balance: Amount, attribs: PortfolAttribs, config: AssetConfig, prevYear: YearlyDetail?,
    ): AssetChange {
        val gainAmount = calcGrossGains(balance, attribs, prevYear)
        val dividends = attribs.divid * balance
        val netNonDivGains = gainAmount - dividends

        val taxableNonDivGains =
            if (netNonDivGains >= 0) Pair(
                netNonDivGains * regTaxOnGainsPct,
                netNonDivGains * ltTaxOnGainsPct
            )
            else Pair(
                netNonDivGains * (1 -qualDivRatio),
                netNonDivGains * qualDivRatio
            )

        val regTaxable = dividends * (1 - qualDivRatio) + taxableNonDivGains.first
        val ltTaxable = dividends * qualDivRatio + taxableNonDivGains.second
        val unrealized = gainAmount - regTaxable - ltTaxable

        return AssetChange(
            name = attribs.name,
            amount = gainAmount,
            unrealized = unrealized,
            taxable = TaxableAmounts(
                person = config.person,
                fed = regTaxable,
                fedLTG = ltTaxable,
                state = regTaxable + ltTaxable)

        )
    }
}