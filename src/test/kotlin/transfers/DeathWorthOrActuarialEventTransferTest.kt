package transfers

import RecIdentifier
import asset.assetRecFixture
import config.configFixture
import departed.ActuarialEvent
import departed.DepartedRec
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class DeathWorthOrActuarialEventTransferTest : ShouldSpec({
    val year = currentDate.year + 1

    val person = "Person"
    val balance = 10000.0
    val assetIdent = RecIdentifier(name = "Asset", person = person)
    val assetRec = assetRecFixture(year, assetIdent, startBal = balance)

    val depositAcctStartBal = 25000.0
    val depositAcctIdent = RecIdentifier(name = "DepositAcct", person = person)
    val depositAcctRec = assetRecFixture(year, depositAcctIdent, startBal = depositAcctStartBal)

    val threshold = balance * 10
    val otherAssetIdent = RecIdentifier("OtherAsset", "Person")
    val otherAssetRec = assetRecFixture(year, otherAssetIdent, startBal = threshold * 3)

    val cmpdInflation = 2.0
    val inflationRec = inflationRecFixture(
        stdRAC = InflationRAC(
            rate = .03, cmpdStart = cmpdInflation - .03, cmpdEnd = cmpdInflation))

    val eventOccurs = ActuarialEvent {_ -> true}
    val eventDoesntOccur = ActuarialEvent {_ -> false}

    val transferName = "Transfer"
    val transferNoEvent = DeathWorthOrActuarialEventTransfer(person, threshold, eventDoesntOccur,
        transferName = transferName, assetIdent, depositAcctIdent)

    val transferWithEvent = DeathWorthOrActuarialEventTransfer(person, threshold, eventOccurs,
        transferName = transferName, assetIdent, depositAcctIdent)

    val config = configFixture()

    should("determineTransferAmount should return 0 is assetRec not found") {
        val currYear = yearlyDetailFixture(year, inflationRec,
            assets = listOf(otherAssetRec, depositAcctRec))

        transferWithEvent.determineTransferAmount(config, currYear).shouldBeZero()
    }

    should("determineTransferAmount should return 0 if event doesn't occur, total other assets over threshold and person not departed") {
        val currYear = yearlyDetailFixture(year, inflationRec,
            assets = listOf(assetRec, otherAssetRec, depositAcctRec))

        transferNoEvent.determineTransferAmount(config, currYear).shouldBeZero()
    }

    should("determineTransferAmount should return value of asset if event occurs") {
        val currYear = yearlyDetailFixture(year, inflationRec,
            assets = listOf(assetRec, otherAssetRec, depositAcctRec))

        transferWithEvent.determineTransferAmount(config, currYear).shouldBe(balance)
    }

    should("determineTransferAmount should return value of asset if person is departed") {
        val currYear = yearlyDetailFixture(year, inflationRec,
            assets = listOf(assetRec, otherAssetRec, depositAcctRec),
            departed = listOf(DepartedRec(person, year - 1)))

        transferNoEvent.determineTransferAmount(config, currYear).shouldBe(balance)
    }

    should("determineTransferAmount should return 0 if assets under threshold") {
        val currYear = yearlyDetailFixture(year, inflationRec,
            assets = listOf(assetRec, depositAcctRec))

        transferNoEvent.determineTransferAmount(config, currYear).shouldBe(balance)
    }

    should("generateTransfers doesn't generate transfer is deposit account doesn't exist") {
        val currYear = yearlyDetailFixture(year, inflationRec,
            assets = listOf(assetRec, otherAssetRec))

        transferWithEvent.generateTransfers(currYear, balance).shouldBeEmpty()
    }


    should("generateTransfers generate a transfer of the sale of the asset into the deposit account") {
        val currYear = yearlyDetailFixture(year, inflationRec,
            assets = listOf(assetRec, otherAssetRec, depositAcctRec))

        val result = transferWithEvent.generateTransfers(currYear, balance)
        result.shouldNotBeEmpty()
        result.shouldHaveSize(1)
        result[0].sourceRec.shouldBe(assetRec)
        result[0].sourceTribution.name.shouldBe(transferName)
        result[0].sourceTribution.amount.shouldBe(-balance)
        result[0].destRec.shouldBe(depositAcctRec)
        result[0].destTribution.name.shouldBe(transferName)
        result[0].destTribution.amount.shouldBe(balance)
    }
})
