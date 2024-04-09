package netspend

import Amount
import YearlyDetail
import asset.AssetChange
import asset.AssetRec
import tax.TaxableAmounts

class TaxableInvestSpendAllocHandler(minAcctBal: Amount = 0.0) : BasicSpendAlloc(minAcctBal) {

    override fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount {
        val drawAmount = Math.min(amount, maxWithdraw(assetRec))
        val stUnrealized = Math.min(
            drawAmount, assetRec.totalUnrealized() - assetRec.startUnrealized
        )
        val ltUnrealized = Math.min(
            drawAmount - stUnrealized, assetRec.startUnrealized
        )
        val taxableAmounts = TaxableAmounts(
            person = assetRec.ident.person,
            fed = stUnrealized,
            fedLTG = ltUnrealized,
            state = stUnrealized + ltUnrealized
        )
        assetRec.tributions.add(
            AssetChange(
            name = "Withdraw",
            amount = -drawAmount,
            unrealized = -stUnrealized -ltUnrealized,
            taxable = taxableAmounts,
            isCarryOver = true
        ))
        return drawAmount
    }
}