package transfers

import Name
import RecIdentifier
import tax.TaxabilityProfile

class CloseAccountsOnDeparture(
    val person: Name,
    val transferName: Name,
    override val sourceDestPairs: List<Pair<RecIdentifier, RecIdentifier>>,
    override val taxabilityProfile: TaxabilityProfile,
) : TransferGenerator, OnDepartureAmountDeterminer, CloseSourceAccountsTransfer,
    TransferRecGenerator by SimpleTransferRecGenerator(transferName, taxabilityProfile) {

    override fun departedPerson(): Name = person
    override fun transferName(): String = transferName
}