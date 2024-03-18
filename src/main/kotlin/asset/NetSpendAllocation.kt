package asset

import Amount
import YearlyDetail

object NetSpendAllocation {

    fun allocatedNetSpend(currYear: YearlyDetail): Amount {
        return AssetType.entries.fold(currYear.netSpend()) { acc, assetType ->
            if (acc < 1.0 && acc > -1.0) 0.0
            else {
                acc + allocateSingleType(
                    amount = acc,
                    assets = currYear.assets.filter { it.config.type == assetType },
                )
            }
        }
    }

    private fun allocateSingleType(amount: Amount, assets: List<AssetRec>): Amount {
        val combinedBalance = assets.sumOf { it.finalBalance() }
        if (combinedBalance <= 0.0) return 0.0

        return assets.sumOf {
            val finalBalance = it.finalBalance()
            val tributionAmount =
                Math.max(
                    -finalBalance,
                    (finalBalance) / combinedBalance * amount
                )

            if (tributionAmount < 1.0 && tributionAmount > -1.0) 0.0
            else {
                it.tributions.add(AssetChange("CoverSpend", tributionAmount))
                -tributionAmount
            }
        }
    }
}