package asset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import tax.NonWageTaxableProfile
import util.currentDate

class FixedPctGainCreatorTest : ShouldSpec({
    val year = currentDate.year + 1
    val gainName = "GainName"
    val person = "Person"
    val balance = 1000.0

    should("createGain creates gain proportional to balance ") {
        val resultNoTax = FixedPctGainCreator(0.5, gainName)
            .createGain(year, person, balance, 0.0)
        resultNoTax.amount.shouldBe(balance * 0.5)
        resultNoTax.name.shouldBe(gainName)
        resultNoTax.taxable?.hasAmounts()?.shouldBeFalse()

        val resultWitTax = FixedPctGainCreator(fixedPct = -.05, gainName, NonWageTaxableProfile())
            .createGain(year, person, balance, 0.0)
        resultWitTax.amount.shouldBe(balance * -0.05)
        resultWitTax.name.shouldBe(gainName)
        resultWitTax.taxable?.person.shouldBe(person)
        resultWitTax.taxable?.hasAmounts()?.shouldBeTrue()
    }
})
