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
                    currYear = currYear
                )
            }
        }
    }

    private fun allocateSingleType(
        amount: Amount, assets: List<AssetRec>, currYear: YearlyDetail,
    ): Amount {
        val combinedBalance = assets.sumOf { it.calcValues.finalBal }
        if (combinedBalance <= 0.0) return 0.0

        return assets.sumOf {
            val tributionAmount =
                Math.max(
                    -it.calcValues.finalBal,
                    (it.calcValues.finalBal) / combinedBalance * amount
                )

            if (tributionAmount < 1.0 && tributionAmount > -1.0) 0.0
            else {
                it.tributions.add(SimpleAssetChange("CoverSpend", tributionAmount))
                it.calcValues = AssetCalcValuesRec.create(it, currYear)
                -tributionAmount
            }
        }
    }
}