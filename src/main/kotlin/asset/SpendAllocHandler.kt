package asset

import Amount
import YearlyDetail

interface SpendAllocHandler {
    object TributionNames {
        const val WITHDRAW = "Withdraw"
        const val DEPOSIT = "Deposit"
    }

    fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount
    fun deposit(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount

    fun addWithdrawTribution(amount: Amount, assetRec: AssetRec): Amount {
        assetRec.tributions.add(AssetChange(TributionNames.WITHDRAW, -amount))
        return amount
    }

    fun addDepositTribution(amount: Amount, assetRec: AssetRec): Amount {
        assetRec.tributions.add(AssetChange(TributionNames.DEPOSIT, amount))
        return amount
    }
}

open class BasicSpendAlloc() : SpendAllocHandler {
    override fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount =
        addWithdrawTribution(Math.min(amount, assetRec.finalBalance()), assetRec)

    override fun deposit(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount =
        addDepositTribution(amount, assetRec)
}