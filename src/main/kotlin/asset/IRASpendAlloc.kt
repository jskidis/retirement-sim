package asset

import Amount
import YearlyDetail
import config.Person
import tax.TaxableAmounts

class IRASpendAlloc(val person: Person) : SpendAllocHandler {
    override fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount {
        val withdrawAmount = Math.min(amount, assetRec.finalBalance())
        val taxable = TaxableAmounts(
            person = person.name,
            state = withdrawAmount,
            fed = withdrawAmount *
                if (currYear.year - person.birthYM.year < 59) 1.1 else 1.0
        )
        return addWithdrawTribution(amount = Math.min(amount, assetRec.finalBalance()),
            assetRec, taxable, isCarryOver = true)
    }

    override fun deposit(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount = 0.0
}