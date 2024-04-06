package asset

import Amount
import Name
import RecIdentifier
import Year
import util.currentDate

fun assetRecFixture(
    year: Year =  currentDate.year + 1,
    ident: RecIdentifier = RecIdentifier(name = "Asset Name", person = "Person"),
    startBal: Amount = 0.0,
    gains: AssetChange = AssetChange("Gain", amount = 0.0),
    startUnrealized: Amount = 0.0,
) = AssetRec(
        year = year,
        ident = ident,
        startBal = startBal,
        startUnrealized = startUnrealized,
        gains = gains
    )

fun assetProgressionFixture(
    name: String = "Asset Name",
    person: String = "Person",
    startBal: Amount = 0.0,
    gains: Amount = 0.0
) : AssetProgression =
    AssetProgression(
        ident = RecIdentifier(name, person),
        startBalance = startBal,
        gainCreator = GainCreatorFixture(name, gains),
    )

class GainCreatorFixture(val name: Name, val gains: Amount): AssetGainCreator {
    override fun createGain(
        year: Year,
        person: Name,
        balance: Amount,
        gaussianRnd: Double,
    ): AssetChange = AssetChange(name, gains)
}
