package asset
import Amount
import Name
import Year
import YearlyDetail

interface AssetGainCreator {
    fun createGain(
        year: Year,
        person: Name,
        balance: Amount,
        gaussianRnd: Double,
    ): AssetChange
}

interface GrossGainsCalc {
    fun calcGrossGains(
        balance: Amount,
        attribs: PortfolioAttribs,
        gaussianRnd: Double,
    ): Amount = balance * (attribs.mean + (attribs.stdDev * gaussianRnd))
}

interface CashFlowEventHandler {
    fun generateCashFlowTribution(assetRec: AssetRec, currYear: YearlyDetail): AssetChange?
}



