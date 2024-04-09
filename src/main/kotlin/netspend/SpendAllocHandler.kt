package netspend

import Amount
import YearlyDetail
import asset.AssetChange
import asset.AssetRec
import tax.TaxableAmounts

interface SpendAllocHandler {
    object TributionNames {
        const val WITHDRAW = "Withdraw"
        const val DEPOSIT = "Deposit"
    }

    fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount
    fun deposit(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount

    fun addWithdrawTribution(
        amount: Amount, assetRec: AssetRec,
        taxable: TaxableAmounts? = null,
        isCarryOver: Boolean = false
    ): Amount {
        assetRec.tributions.add(
            AssetChange(name= TributionNames.WITHDRAW,
            amount = -amount, taxable = taxable, isCarryOver = isCarryOver))
        return amount
    }

    fun addDepositTribution(
        amount: Amount, assetRec: AssetRec,
        taxable: TaxableAmounts? = null,
        isCarryOver: Boolean = false
    ): Amount {
        assetRec.tributions.add(
            AssetChange(name = TributionNames.DEPOSIT,
            amount = amount, taxable = taxable, isCarryOver = isCarryOver))
        return amount
    }
}

open class BasicSpendAlloc(val minBal: Amount = 0.0) : SpendAllocHandler {
    override fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount =
        addWithdrawTribution(Math.min(amount, maxWithdraw(assetRec)), assetRec)

    fun maxWithdraw(assetRec: AssetRec): Amount =
        assetRec.finalBalance() - minBal

    override fun deposit(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount =
        addDepositTribution(amount, assetRec)
}