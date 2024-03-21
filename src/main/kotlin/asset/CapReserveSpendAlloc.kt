package asset

import Amount
import Year
import YearlyDetail

open class CapReserveSpendAlloc(
    val yearlyTargetMult: List<Pair<Year, Double>>,
    val margin: Double,
) : SpendAllocHandler {

    override fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount {
        val target = determineTarget(currYear)
        val floor = target * (1 - margin)
        val assetBalance = assetRec.finalBalance()

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
        val target = determineTarget(currYear)
        val ceiling = target * (1 + margin)
        val assetBalance = assetRec.finalBalance()

        return when {
            // asset over target ceiling, so make a withdrawl instead which increasing amount needed to deposit
            assetBalance > ceiling -> -addWithdrawTribution(assetBalance - target, assetRec)
            // asset over target but not under ceiling, don't make any deposit
            assetBalance >= target -> 0.0
            // deposit up to target or amount requested which ever is least
            else -> addDepositTribution(Math.min(amount, target - assetBalance), assetRec)
        }
    }

    open fun determineTarget(currYear: YearlyDetail): Amount {
        val multiplier = getMultiplierForYear(currYear.year)
        val netExpenses = currYear.totalExpense() - currYear.totalBenefits()
        return multiplier * netExpenses
    }

    open fun getMultiplierForYear(year: Year): Double =
        (yearlyTargetMult.findLast { year >= it.first } ?: yearlyTargetMult.first()).second
}