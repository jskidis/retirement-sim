package asset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import util.YearBasedConfig
import util.YearConfigPair
import util.currentDate

class TaxableInvestGainCreatorTest : ShouldSpec({
    val year = currentDate.year +1
    val balance = 1000.0

    fun buildAttributeSet(attributes: PortfolioAttribs): YearBasedConfig<PortfolioAttribs> =
        YearBasedConfig(
            listOf(YearConfigPair(year, attributes))
        )

    should("Apportion dividends between short term and long term according to qualified div ratio") {
        val attributes = PortfolioAttribs(name = "10/1", mean = 0.1, stdDev = 0.0, divid = 0.01)

        val creator = TaxableInvestGainCreator(buildAttributeSet(attributes),
            qualDivRatio = 0.80, regTaxOnGainsPct = 0.0, ltTaxOnGainsPct = 0.0)

        val result = creator.createGain(year = year, person = "Person", balance = balance, gaussianRnd = 0.0)
        result.name.shouldBe(attributes.name)
        result.amount.shouldBe(balance * attributes.mean)

        val fedLtTaxable = result.taxable?.fedLTG ?: 0.0
        val fedRegTaxable = result.taxable?.fed ?: 0.0
        result.unrealized.shouldBeWithinPercentageOf(
            result.amount - fedLtTaxable - fedRegTaxable, .001)
        fedLtTaxable.shouldBeWithinPercentageOf(balance * attributes.divid * 0.8, .001)
        fedRegTaxable.shouldBeWithinPercentageOf(balance * attributes.divid * 0.2, .001)
    }

    should("Apportions taxability of non dividend gains according to pcts of regular and long-term") {
        val attributes = PortfolioAttribs(name = "10/1", mean = 0.1, stdDev = 0.0, divid = 0.00)

        val creator = TaxableInvestGainCreator(buildAttributeSet(attributes),
            qualDivRatio = 0.00, regTaxOnGainsPct = 0.2, ltTaxOnGainsPct = 0.3)

        val result = creator.createGain(year = year, person = "Person", balance = balance, gaussianRnd = 0.0)
        result.name.shouldBe(attributes.name)
        result.amount.shouldBe(balance * attributes.mean)

        val fedLtTaxable = result.taxable?.fedLTG ?: 0.0
        val fedRegTaxable = result.taxable?.fed ?: 0.0
        result.unrealized.shouldBeWithinPercentageOf(
            result.amount - fedLtTaxable - fedRegTaxable, .001)
        fedLtTaxable.shouldBeWithinPercentageOf(result.amount * 0.3, .001)
        fedRegTaxable.shouldBeWithinPercentageOf(result.amount * 0.2, .001)
    }

    should("Apportions taxability of both dividends and non dividend gains") {
        val attributes = PortfolioAttribs(name = "10/1", mean = 0.10, stdDev = 0.0, divid = 0.05)

        val creator = TaxableInvestGainCreator(buildAttributeSet(attributes),
            qualDivRatio = 0.80, regTaxOnGainsPct = 0.2, ltTaxOnGainsPct = 0.3)

        val result = creator.createGain(year = year, person = "Person", balance = balance, gaussianRnd = 0.0)
        result.name.shouldBe(attributes.name)
        result.amount.shouldBe(balance * attributes.mean)

        val fedLtTaxable = result.taxable?.fedLTG ?: 0.0
        val expectedLtTaxable = balance * attributes.divid * 0.8 +
            balance * (attributes.mean - attributes.divid) * 0.3
        fedLtTaxable.shouldBeWithinPercentageOf(expectedLtTaxable, .001)

        val fedRegTaxable = result.taxable?.fed ?: 0.0
        val expectedRegTaxable = balance * attributes.divid * 0.2 +
            balance * (attributes.mean - attributes.divid) * 0.2
        fedRegTaxable.shouldBeWithinPercentageOf(expectedRegTaxable, .001)
    }

    should("Apportions taxability when rate is less than dividend (gains are net negative) by doing tax loss capture") {
        val attributes = PortfolioAttribs(name = "10/1", mean = -0.1, stdDev = 0.0, divid = 0.01)

        val creator = TaxableInvestGainCreator(buildAttributeSet(attributes),
            qualDivRatio = 0.80, regTaxOnGainsPct = 0.2, ltTaxOnGainsPct = 0.3)

        val result = creator.createGain(year = year, person = "Person", balance = balance, gaussianRnd = 0.0)
        result.name.shouldBe(attributes.name)
        result.amount.shouldBe(balance * attributes.mean)

        val fedLtTaxable = result.taxable?.fedLTG ?: 0.0
        val expectedLtTaxable = balance * attributes.divid * 0.8 +
            balance * (attributes.mean - attributes.divid) * 0.8
        fedLtTaxable.shouldBeWithinPercentageOf(expectedLtTaxable, .001)

        val fedRegTaxable = result.taxable?.fed ?: 0.0
        val expectedRegTaxable = balance * attributes.divid * 0.2 +
            balance * (attributes.mean - attributes.divid) * 0.2
        fedRegTaxable.shouldBeWithinPercentageOf(expectedRegTaxable, .001)
        result.unrealized.shouldBe(0.0)
    }
})
