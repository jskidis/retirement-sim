package asset

import Amount
import Name
import Year
import tax.TaxableAmounts
import util.YearBasedConfig

open class TaxableInvestGainCreator(
    val attributesSet: YearBasedConfig<PortfolioAttribs>,
    val qualDivRatio: Double = 0.8,
    val regTaxOnGainsPct: Double = 0.1,
    val ltTaxOnGainsPct: Double = 0.1,
) : AssetGainCreator, GrossGainsCalc {

    override fun createGain(
        year: Year,
        person: Name,
        balance: Amount,
        gaussianRnd: Double,
    ): AssetChange {

        val attributes = attributesSet.getConfigForYear(year)
        val gainAmount = calcGrossGains(balance, attributes, gaussianRnd)
        val dividends = attributes.divid * balance
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
            name = attributes.name,
            amount = gainAmount,
            unrealized = unrealized,
            taxable = TaxableAmounts(
                person = person,
                fed = regTaxable,
                fedLTG = ltTaxable,
                state = regTaxable + ltTaxable)

        )
    }
}