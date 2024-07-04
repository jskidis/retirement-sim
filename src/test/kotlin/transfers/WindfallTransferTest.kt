package transfers

import RecIdentifier
import asset.assetRecFixture
import config.configFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class WindfallTransferTest : ShouldSpec({
    val person = "Person"
    val accountName = "DestAccount"

    val windfallAmnt = 500000.0
    val windfallName = "Windfall"

    val thisYear = currentDate.year
    val windfallYear = currentDate.year + 1

    val destAcctIdent = RecIdentifier(accountName, person)
    val destAccount = assetRecFixture(windfallYear, destAcctIdent)

    val windfallTransfer = WindfallTransfer(
        amount = windfallAmnt,
        year = windfallYear,
        destAcctIdent = destAcctIdent,
        transferName = windfallName,
    )

    val simConfg = configFixture(thisYear)

    should("transfer amount should be empty if year is not windfall year") {
        val yearNoDepart = yearlyDetailFixture(thisYear, assets = listOf(destAccount))
        windfallTransfer.determineTransferAmount(simConfg, yearNoDepart).shouldBeZero()
    }

    should("transfer amount of windfall to dest account if year is windfall year") {
        val currYear = yearlyDetailFixture(windfallYear, assets = listOf(destAccount))

        val amount = windfallTransfer.determineTransferAmount(simConfg, currYear)
        amount.shouldBe(amount)

        val transfers = windfallTransfer.generateTransfers(currYear, amount)
        transfers.shouldHaveSize(1)

        transfers[0].destRec.ident.shouldBe(destAcctIdent)
        transfers[0].destTribution.name.shouldBe(windfallName)
        transfers[0].destTribution.amount.shouldBe(amount)
    }
})
