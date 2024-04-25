package netspend

import Amount
import RecIdentifier
import YearlyDetail
import util.PortionOfYearPast
import util.RecFinder

object NetSpendAllocation {

    fun allocateNetSpend(
        netSpend: Amount,
        currYear: YearlyDetail,
        config: NetSpendAllocationConfig,
    ): Amount =
        if (netSpend < -1.0) processesWithdraws(netSpend, currYear, config.withdrawOrder)
        else if (netSpend > 1.0) processesDeposits(netSpend, currYear, config.depositOrder)
        else 0.0

    fun determineNetSpend(currYear: YearlyDetail, prevYear: YearlyDetail?): Amount {
        val carryOverTaxes = prevYear?.let {
            (it.finalPassTaxes.total() - it.taxes.total()) * (1 + currYear.inflation.std.rate)
        } ?: 0.0
        val netSpend = (currYear.totalIncome() + currYear.totalBenefits() -
            currYear.totalExpense() + currYear.totalAssetCashflow() -
            currYear.taxes.total() - carryOverTaxes)
        return (1- PortionOfYearPast.calc(currYear.year)) * netSpend
    }

    private fun processesWithdraws(
        netSpend: Amount,
        currYear: YearlyDetail,
        withdrawOrder: List<NetSpendAssetConfig>,
    ): Amount =
        withdrawOrder.fold(netSpend) { acc, it ->
            if (acc > -1.0) 0.0
            else {
                val assetRec = RecFinder.findAssetRec(it.ident, currYear)
                if (assetRec == null) acc
                else acc + it.spendAllocHandler.withdraw(-acc, assetRec, currYear)
            }
        }

    private fun processesDeposits(
        netSpend: Amount,
        currYear: YearlyDetail,
        depositOrder: List<NetSpendAssetConfig>,
    ): Amount =
        depositOrder.fold(netSpend) { acc, it ->
            if (acc < 1.0) 0.0
            else {
                val assetRec = RecFinder.findAssetRec(it.ident, currYear)
                if (assetRec == null) acc
                else acc - it.spendAllocHandler.deposit(acc, assetRec, currYear)
            }
        }
}

data class NetSpendAllocationConfig(
    val withdrawOrder: List<NetSpendAssetConfig>,
    val depositOrder: List<NetSpendAssetConfig>
)

data class NetSpendAssetConfig(
    val ident: RecIdentifier,
    val spendAllocHandler: SpendAllocHandler
)

