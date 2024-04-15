package asset
import Amount
import Name
import Year

fun interface AssetGainCreator {
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

enum class AssetType {
    CASH, NRA, IRA, ROTH, STD401K, ROTH401K, OTHER
}



