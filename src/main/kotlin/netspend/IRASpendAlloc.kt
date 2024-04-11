package netspend

import Amount
import YearlyDetail
import asset.AssetRec
import config.Person
import tax.NonTaxableProfile
import tax.TaxabilityProfile
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.RETIREMENT_WITHDRAW_AGE

open class IRASpendAlloc(
    val person: Person,
    val taxabilityProfile: TaxabilityProfile,
)
    : SpendAllocHandler {
    override fun withdraw(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount {
        val withdrawAmount = Math.min(amount, assetRec.finalBalance())

        val baseTaxable = taxabilityProfile.calcTaxable(person.name, withdrawAmount)
        val taxableIncPenalty = baseTaxable.copy(
            fed = baseTaxable.fed + (withdrawAmount * penalty(currYear, person))
        )
        val taxable = if (taxableIncPenalty.hasAmounts()) taxableIncPenalty else null
        return addWithdrawTribution(
            amount = Math.min(amount, assetRec.finalBalance()),
            assetRec = assetRec,
            taxable = taxable,
            isCarryOver = true
        )
    }

    private fun penalty(currYear: YearlyDetail, person: Person): Amount =
        if (currYear.year + 1 - person.birthYM.toDec() <
            ConstantsProvider.getValue(RETIREMENT_WITHDRAW_AGE)
        ) 0.1
        else 0.0

    override fun deposit(amount: Amount, assetRec: AssetRec, currYear: YearlyDetail): Amount = 0.0
}

class RothSpendAlloc(person: Person)
    : IRASpendAlloc(person, NonTaxableProfile()) {
}