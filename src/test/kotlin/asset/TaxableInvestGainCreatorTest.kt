package asset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe

class TaxableInvestGainCreatorTest : ShouldSpec({
    val config = assetConfigFixture("Person")
    val balance = 1000.0

    should("Apportion dividends between short term and long term according to qualified div ratio") {
        val portAttribs = PortfolAttribs(name = "10/1", mean = 0.1, stdDev = 0.0, divid = 0.01)

        val creator = TaxableInvestGainCreator(
            qualDivRatio = 0.80, regTaxOnGainsPct = 0.0, ltTaxOnGainsPct = 0.0)

        val result = creator.createGain(balance, portAttribs, config, null)
        result.name.shouldBe(portAttribs.name)
        result.totalAmount().shouldBe(balance * portAttribs.mean)
        result.ltTaxable.shouldBeWithinPercentageOf(balance * portAttribs.divid * 0.8, .001)
        result.regTaxable.shouldBeWithinPercentageOf(balance * portAttribs.divid * 0.2, .001)
        result.unrealized.shouldBeWithinPercentageOf(
            result.totalAmount() - result.ltTaxable - result.regTaxable, .001)
    }

    should("Apportions taxability of non dividend gains according to pcts of regular and long-term") {
        val portAttribs = PortfolAttribs(name = "10/1", mean = 0.1, stdDev = 0.0, divid = 0.00)

        val creator = TaxableInvestGainCreator(
            qualDivRatio = 0.00, regTaxOnGainsPct = 0.2, ltTaxOnGainsPct = 0.3)

        val result = creator.createGain(balance, portAttribs, config, null)
        result.name.shouldBe(portAttribs.name)
        result.totalAmount().shouldBe(balance * portAttribs.mean)
        result.ltTaxable.shouldBeWithinPercentageOf(result.totalAmount() * 0.3, .001)
        result.regTaxable.shouldBeWithinPercentageOf(result.totalAmount() * 0.2, .001)
        result.unrealized.shouldBeWithinPercentageOf(
            result.totalAmount() - result.ltTaxable - result.regTaxable, .001)
    }

    should("Apportions taxability of both dividends and non dividend gains") {
        val portAttribs = PortfolAttribs(name = "10/1", mean = 0.10, stdDev = 0.0, divid = 0.05)

        val creator = TaxableInvestGainCreator(
            qualDivRatio = 0.80, regTaxOnGainsPct = 0.2, ltTaxOnGainsPct = 0.3)

        val result = creator.createGain(balance, portAttribs, config, null)
        result.name.shouldBe(portAttribs.name)
        result.totalAmount().shouldBe(balance * portAttribs.mean)

        val expectedLtTaxable = balance * portAttribs.divid * 0.8 +
            balance * (portAttribs.mean - portAttribs.divid) * 0.3
        result.ltTaxable.shouldBeWithinPercentageOf(expectedLtTaxable, .001)

        val expectedRegTaxable = balance * portAttribs.divid * 0.2 +
            balance * (portAttribs.mean - portAttribs.divid) * 0.2
        result.regTaxable.shouldBeWithinPercentageOf(expectedRegTaxable, .001)

        result.unrealized.shouldBeWithinPercentageOf(
            result.totalAmount() - result.ltTaxable - result.regTaxable, .001)
    }

    should("Apportions taxability when rate is less than dividend (gains are net negative) by doing tax loss capture") {
        val portAttribs = PortfolAttribs(name = "10/1", mean = -0.1, stdDev = 0.0, divid = 0.01)

        val creator = TaxableInvestGainCreator(
            qualDivRatio = 0.80, regTaxOnGainsPct = 0.2, ltTaxOnGainsPct = 0.3)

        val result = creator.createGain(balance, portAttribs, config, null)
        result.name.shouldBe(portAttribs.name)
        result.totalAmount().shouldBe(balance * portAttribs.mean)

        val expectedLtTaxable = balance * portAttribs.divid * 0.8 +
            balance * (portAttribs.mean - portAttribs.divid) * 0.8
        result.ltTaxable.shouldBeWithinPercentageOf(expectedLtTaxable, .001)

        val expectedRegTaxable = balance * portAttribs.divid * 0.2 +
            balance * (portAttribs.mean - portAttribs.divid) * 0.2
        result.regTaxable.shouldBeWithinPercentageOf(expectedRegTaxable, .001)

        result.unrealized.shouldBe(0.0)
    }
})
