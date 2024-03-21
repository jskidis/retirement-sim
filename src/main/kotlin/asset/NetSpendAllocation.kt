package asset

import Amount
import YearlyDetail

object NetSpendAllocation {

    fun allocateNetSpend(
        netSpend: Amount,
        currYear: YearlyDetail,
        config: NetSpendAllocationConfig,
    ): Amount =
        if (netSpend < -1.0) processesWithdraws(netSpend, currYear, config.withdrawOrder)
        else if (netSpend > 1.0) processesDeposits(netSpend, currYear, config.depositOrder)
        else 0.0

    private fun processesWithdraws(
        netSpend: Amount,
        currYear: YearlyDetail,
        withdrawOrder: List<AssetConfigProgression>,
    ): Amount =
        withdrawOrder.fold(netSpend) { acc, it ->
            if (acc > -1.0) 0.0
            else {
                val assetRec = findAssetRec(currYear, it)
                if (assetRec == null) acc
                else acc + it.withdrawDepositHandler.withdraw(-acc, assetRec, currYear)
            }
        }

    private fun processesDeposits(
        netSpend: Amount,
        currYear: YearlyDetail,
        depositOrder: List<AssetConfigProgression>,
    ): Amount =
        depositOrder.fold(netSpend) { acc, it ->
            if (acc < 1.0) 0.0
            else {
                val assetRec = findAssetRec(currYear, it)
                if (assetRec == null) acc
                else acc - it.withdrawDepositHandler.deposit(acc, assetRec, currYear)
            }
        }

    private fun findAssetRec(
        currYear: YearlyDetail,
        configProgression: AssetConfigProgression,
    ): AssetRec? = currYear.assets.find { it.config == configProgression.config }
}
