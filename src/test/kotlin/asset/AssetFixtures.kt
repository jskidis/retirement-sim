package asset

import Amount
import RecIdentifier
import Year
import YearlyDetail
import tax.TaxCalcConfig
import tax.TaxableAmounts
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
        gainCreator = { _, _, _, _ -> AssetChange(name, gains) }
    )

class RothConversionAmountCalcFixture(val amountToConvert: Amount): RothConversionAmountCalc {
    override fun amountToConvert(
        currYear: YearlyDetail,
        taxableAmounts: TaxableAmounts,
        taxCalcConfig: TaxCalcConfig,
    ): Amount = amountToConvert
}
