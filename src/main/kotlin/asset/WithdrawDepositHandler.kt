package asset

import Amount
import YearlyDetail

interface WithdrawDepositHandler {
    fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount
    fun deposit(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount
}

class BasicWithdrawDeposit() : WithdrawDepositHandler {
    override fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount {
        val drawAmount = Math.min(amount, assetRec.finalBalance())
        assetRec.tributions.add(AssetChange(name = "Withdraw", amount = -drawAmount))
        return drawAmount
    }

    override fun deposit(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount {
        assetRec.tributions.add(AssetChange(name = "Deposit", amount = amount))
        return amount
    }
}