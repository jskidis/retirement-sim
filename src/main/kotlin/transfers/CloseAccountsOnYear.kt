package transfers

import Name
import RecIdentifier
import Year
import tax.TaxabilityProfile

open class CloseAccountsOnYear(
    val year: Year,
    val transferName: Name,
    override val sourceDestPairs: List<Pair<RecIdentifier, RecIdentifier>>,
    override val taxabilityProfile: TaxabilityProfile,
) : TransferGenerator, ByYearAmountDeterminer, CloseSourceAccountsTransfer,
    TransferRecGenerator by SimpleTransferRecGenerator(transferName, taxabilityProfile) {

    override fun yearToTransfer(): Year = year
    override fun transferName(): String = transferName
}