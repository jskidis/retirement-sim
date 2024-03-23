package asset

import Amount
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import tax.NonWageTaxableProfile

class SimpleAssetGainCreatorTest : FunSpec({

    test("createGain applies gain and taxable amounts to Change") {
        val taxableProfile = NonWageTaxableProfile()
        val assetConfig = assetConfigFixture(taxProfile = taxableProfile)
        val portfolioAttribs = PortfolAttribs("PortfolioName", 0.1, 0.0)

        val balance = 1000.0
        val gainAmount = 100.0
        val gainCreator = SimpleAssetGainCreatorFixture(gainAmount)

        val expectedTaxable = taxableProfile.calcTaxable(assetConfig.person, gainAmount)

        val results = gainCreator.createGain(balance, portfolioAttribs, assetConfig, 0.0 )
        results.name.shouldBe(portfolioAttribs.name)
        results.amount.shouldBe(gainAmount)
        results.taxable.shouldBe(expectedTaxable)
        results.isCarryOver.shouldBeFalse()
        results.isReqDist.shouldBeFalse()
    }
})

class SimpleAssetGainCreatorFixture(val gainAmount: Amount) : SimpleAssetGainCreator() {
    override fun calcGrossGains(
        balance: Amount, attribs: PortfolAttribs, gaussianRnd: Double,
    ): Amount = gainAmount
}
