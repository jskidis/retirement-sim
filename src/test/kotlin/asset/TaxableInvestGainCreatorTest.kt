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
        result.amount.shouldBe(balance * portAttribs.mean)

        val fedLtTaxable = result.taxable?.fedLTG ?: 0.0
        val fedRegTaxable = result.taxable?.fed ?: 0.0
        result.unrealized.shouldBeWithinPercentageOf(
            result.amount - fedLtTaxable - fedRegTaxable, .001)
        fedLtTaxable.shouldBeWithinPercentageOf(balance * portAttribs.divid * 0.8, .001)
        fedRegTaxable.shouldBeWithinPercentageOf(balance * portAttribs.divid * 0.2, .001)
    }

    should("Apportions taxability of non dividend gains according to pcts of regular and long-term") {
        val portAttribs = PortfolAttribs(name = "10/1", mean = 0.1, stdDev = 0.0, divid = 0.00)

        val creator = TaxableInvestGainCreator(
            qualDivRatio = 0.00, regTaxOnGainsPct = 0.2, ltTaxOnGainsPct = 0.3)

        val result = creator.createGain(balance, portAttribs, config, null)
        result.name.shouldBe(portAttribs.name)
        result.amount.shouldBe(balance * portAttribs.mean)

        val fedLtTaxable = result.taxable?.fedLTG ?: 0.0
        val fedRegTaxable = result.taxable?.fed ?: 0.0
        result.unrealized.shouldBeWithinPercentageOf(
            result.amount - fedLtTaxable - fedRegTaxable, .001)
        fedLtTaxable.shouldBeWithinPercentageOf(result.amount * 0.3, .001)
        fedRegTaxable.shouldBeWithinPercentageOf(result.amount * 0.2, .001)
    }

    should("Apportions taxability of both dividends and non dividend gains") {
        val portAttribs = PortfolAttribs(name = "10/1", mean = 0.10, stdDev = 0.0, divid = 0.05)

        val creator = TaxableInvestGainCreator(
            qualDivRatio = 0.80, regTaxOnGainsPct = 0.2, ltTaxOnGainsPct = 0.3)

        val result = creator.createGain(balance, portAttribs, config, null)
        result.name.shouldBe(portAttribs.name)
        result.amount.shouldBe(balance * portAttribs.mean)

        val fedLtTaxable = result.taxable?.fedLTG ?: 0.0
        val expectedLtTaxable = balance * portAttribs.divid * 0.8 +
            balance * (portAttribs.mean - portAttribs.divid) * 0.3
        fedLtTaxable.shouldBeWithinPercentageOf(expectedLtTaxable, .001)

        val fedRegTaxable = result.taxable?.fed ?: 0.0
        val expectedRegTaxable = balance * portAttribs.divid * 0.2 +
            balance * (portAttribs.mean - portAttribs.divid) * 0.2
        fedRegTaxable.shouldBeWithinPercentageOf(expectedRegTaxable, .001)
    }

    should("Apportions taxability when rate is less than dividend (gains are net negative) by doing tax loss capture") {
        val portAttribs = PortfolAttribs(name = "10/1", mean = -0.1, stdDev = 0.0, divid = 0.01)

        val creator = TaxableInvestGainCreator(
            qualDivRatio = 0.80, regTaxOnGainsPct = 0.2, ltTaxOnGainsPct = 0.3)

        val result = creator.createGain(balance, portAttribs, config, null)
        result.name.shouldBe(portAttribs.name)
        result.amount.shouldBe(balance * portAttribs.mean)

        val fedLtTaxable = result.taxable?.fedLTG ?: 0.0
        val expectedLtTaxable = balance * portAttribs.divid * 0.8 +
            balance * (portAttribs.mean - portAttribs.divid) * 0.8
        fedLtTaxable.shouldBeWithinPercentageOf(expectedLtTaxable, .001)

        val fedRegTaxable = result.taxable?.fed ?: 0.0
        val expectedRegTaxable = balance * portAttribs.divid * 0.2 +
            balance * (portAttribs.mean - portAttribs.divid) * 0.2
        fedRegTaxable.shouldBeWithinPercentageOf(expectedRegTaxable, .001)
        result.unrealized.shouldBe(0.0)
    }
})
