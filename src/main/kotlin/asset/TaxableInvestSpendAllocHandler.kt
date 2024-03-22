package asset

import Amount
import YearlyDetail
import tax.TaxableAmounts

class TaxableInvestSpendAllocHandler : BasicSpendAlloc() {

    override fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount {
        val drawAmount = Math.min(amount, assetRec.finalBalance())
        val stUnrealized = Math.min(
            drawAmount, assetRec.totalUnrealized() - assetRec.startUnrealized
        )
        val ltUnrealized = Math.min(
            drawAmount - stUnrealized, assetRec.startUnrealized
        )
        val taxableAmounts = TaxableAmounts(
            person = assetRec.config.person,
            fed = stUnrealized,
            fedLTG = ltUnrealized,
            state = stUnrealized + ltUnrealized
        )
        assetRec.tributions.add(AssetChange(
            name = "Withdraw",
            amount = -drawAmount,
            unrealized = -stUnrealized -ltUnrealized,
            taxable = taxableAmounts,
            isCarryOver = true
        ))
        return drawAmount
    }
}