package netspend

import Amount
import RecIdentifier
import YearlyDetail
import asset.AssetRec
import util.YearBasedConfig

open class CapReserveSpendAlloc(
    val yearlyTargetMult: YearBasedConfig<Double>,
    val margin: Double,
    val excludedAssets: List<RecIdentifier> = ArrayList()
) : SpendAllocHandler {

    override fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount {
        val assetBalance = assetRec.finalBalance()
        val target = determineTarget(currYear, assetRec)
        val floor = target * (1 - margin)

        return when {
            // asset under target floor, so make a deposit instead which increasing amount needed to withdraw
            assetBalance < floor -> -addDepositTribution(target - assetBalance, assetRec)
            // asset under target but not under floor, don't make any withdraw
            assetBalance <= target -> 0.0
            // withdraw down to target or amount requested which ever is least
            else -> addWithdrawTribution(Math.min(amount, assetBalance - target), assetRec)
        }
    }

    override fun deposit(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount {
        val assetBalance = assetRec.finalBalance()
        val target = determineTarget(currYear, assetRec)
        val ceiling = target * (1 + margin)

        return when {
            // asset over target ceiling, so make a withdrawl instead which increasing amount needed to deposit
            assetBalance > ceiling -> -addWithdrawTribution(assetBalance - target, assetRec)
            // asset over target but not under ceiling, don't make any deposit
            assetBalance >= target -> 0.0
            // deposit up to target or amount requested which ever is least
            else -> addDepositTribution(Math.min(amount, target - assetBalance), assetRec)
        }
    }

    open fun determineTarget(currYear: YearlyDetail, assetRec: AssetRec): Amount {
        val netExpenses = currYear.totalExpense() - currYear.totalBenefits()
        val totalAssets = currYear.totalAssetValues(excludedAssets + assetRec.ident)
        return Math.min(Math.max(0.0, totalAssets - netExpenses),
            yearlyTargetMult.getConfigForYear(currYear.year) * netExpenses)
    }
}