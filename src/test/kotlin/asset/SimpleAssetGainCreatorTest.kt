package asset

import Amount
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import tax.NonWageTaxableProfile
import tax.TaxabilityProfile
import util.SingleYearBasedConfig
import util.currentDate

class SimpleAssetGainCreatorTest : ShouldSpec({

    should("createGain applies gain and taxable amounts to Change") {
        val year = currentDate.year + 1
        val taxableProfile = NonWageTaxableProfile()
        val portfolioAttribs = PortfolioAttribs("PortfolioName", 0.1, 0.0)

        val balance = 1000.0
        val gainAmount = 100.0
        val gainCreator =
            SimpleAssetGainCreatorFixture(gainAmount, taxableProfile, portfolioAttribs)

        val expectedTaxable = taxableProfile.calcTaxable("Person", gainAmount)

        val results = gainCreator.createGain(year, "Person", balance, 0.0)

        results.name.shouldBe(portfolioAttribs.name)
        results.amount.shouldBe(gainAmount)
        results.taxable.shouldBe(expectedTaxable)
        results.isCarryOver.shouldBeFalse()
        results.cashflow.shouldBeZero()
    }
})

class SimpleAssetGainCreatorFixture(
    val gainAmount: Amount,
    taxability: TaxabilityProfile,
    attributes: PortfolioAttribs,
) : SimpleAssetGainCreator(
    taxability,
    SingleYearBasedConfig(attributes)
) {
    override fun calcGrossGains(
        balance: Amount, attribs: PortfolioAttribs, gaussianRnd: Double,
    ): Amount = gainAmount
}


