package transfers

import RecIdentifier
import asset.assetRecFixture
import config.configFixture
import departed.DepartedRec
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class LifeInsuranceTest : ShouldSpec({

    val person = "Person"
    val policyName = "LifeInsurance"
    val coverageAmnt = 500000.0

    val startYear = currentDate.year
    val endYear = startYear + 10
    val coverageRange = IntRange(startYear, endYear)

    val destAcctIdent = RecIdentifier("DestAccount", person)
    val destAccount = assetRecFixture(startYear, destAcctIdent)

    val lifeInsurance = LifeInsurance(
        policyName, person,
        coverageAmnt, coverageRange, destAcctIdent)

    val simConfg = configFixture(startYear)

    should("transfer amount of coverage to dest account if person departed this year and coverage still exists") {
        val currYear = yearlyDetailFixture(
            startYear + 1,
            assets = listOf(destAccount), departed = listOf(DepartedRec(person, startYear + 1)))

        val amount = lifeInsurance.determineTransferAmount(simConfg, currYear)
        amount.shouldBe(coverageAmnt)

        val transfers = lifeInsurance.generateTransfers(currYear, amount)
        transfers.shouldHaveSize(1)

        transfers[0].destRec.ident.shouldBe(destAcctIdent)
        transfers[0].destTribution.name.shouldBe(policyName)
        transfers[0].destTribution.amount.shouldBe(amount)
    }

    should("transfer amount should be empty if person didn't depart this year") {
        // No Departure
        val yearNoDepart = yearlyDetailFixture(
            startYear,
            assets = listOf(destAccount), departed = listOf())

        lifeInsurance.determineTransferAmount(simConfg, yearNoDepart).shouldBeZero()

        // Departed in the past
        val departedYearsAgo = yearlyDetailFixture(
            startYear,
            assets = listOf(destAccount), departed = listOf(DepartedRec(person, startYear - 5)))

        lifeInsurance.determineTransferAmount(simConfg, departedYearsAgo).shouldBeZero()
    }

    should("transfer amount should be empty if coverage has lapsed or hasn't started") {
        // After Lapse
        val yearAfterLapse = yearlyDetailFixture(
            endYear + 1,
            assets = listOf(destAccount), departed = listOf(DepartedRec(person, endYear + 1)))

        lifeInsurance.determineTransferAmount(simConfg, yearAfterLapse).shouldBeZero()

        // Before start
        val yearBeforeStart = yearlyDetailFixture(
            startYear - 1,
            assets = listOf(destAccount), departed = listOf(DepartedRec(person, startYear - 1)))

        lifeInsurance.determineTransferAmount(simConfg, yearBeforeStart).shouldBeZero()
    }
})
