package expense

import RecIdentifier
import asset.assetRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class AssetStillOwnedAdjusterTest : ShouldSpec({

    val year = currentDate.year + 1
    val amount = 10000.0
    val accountIdent = RecIdentifier(name = "TheRec", person = "Person")
    val adjuster = AssetStillOwnedAdjuster(accountIdent)

    should("adjustAmount returns 0 if asset not found") {
        val prevYear = yearlyDetailFixture(year, assets = listOf())
        adjuster.adjustAmount(amount, prevYear).shouldBeZero()
    }

    should("adjustAmount return 0 if asset found but has no balance") {
        val assetRec = assetRecFixture(year, ident = accountIdent, startBal = 0.0)
        val prevYear = yearlyDetailFixture(year, assets = listOf(assetRec))
        adjuster.adjustAmount(amount, prevYear).shouldBeZero()
    }

    should("adjustAmount returns amount if asset found and asset has a balance") {
        val assetRec = assetRecFixture(year, ident = accountIdent, startBal = 500000.0)
        val prevYear = yearlyDetailFixture(year, assets = listOf(assetRec))
        adjuster.adjustAmount(amount, prevYear).shouldBe(amount)
    }
})
