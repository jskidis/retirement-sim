package socsec

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.SSTRUSTFUND_DEPLETED_YEAR
import util.RandomizerFactory

class RandomizedSSPayoutTest : ShouldSpec({
    val year = ConstantsProvider.getValue(SSTRUSTFUND_DEPLETED_YEAR).toInt() + 1
    val stdPayout = 25000.0

    should("adjustPayout is always payout amount if supressRandom is one") {
        RandomizerFactory.setSuppressRandom(true)
        RandomizedSSPayout().adjustPayout(stdPayout, year).shouldBe(stdPayout)
    }

    should("once random value established, payout pct should always be the same") {
        RandomizerFactory.setSuppressRandom(false)
        val payoutProvider = RandomizedSSPayout()
        val initialPayout = payoutProvider.adjustPayout(stdPayout, year)

        repeat(1000) {
            payoutProvider.adjustPayout(initialPayout, year)
        }
    }

    should("should be 100% roughly 1/2 the time, 83% roughly 1/4 the time, and in between the rest") {
        RandomizerFactory.setSuppressRandom(false)
        val numRuns = 20000
        val results = (1 .. numRuns).map {
            RandomizedSSPayout().adjustPayout(stdPayout, year) / stdPayout
        }

        val pctFullPayout = results.filter{ it >= 1.00 }.size * 1.0 / results.size
        val pctLowPayout = results.filter { it <= 0.84 }.size * 1.0 / results.size

        pctFullPayout.shouldBeWithinPercentageOf(0.50, 5.0)
        pctLowPayout.shouldBeWithinPercentageOf(0.25, 10.0)
    }

    should("always return stdPayout if year is prior to ss trustfund depletion year") {
        RandomizerFactory.setSuppressRandom(false)
        val earlyYear = ConstantsProvider.getValue(SSTRUSTFUND_DEPLETED_YEAR).toInt() - 1

        val numRuns = 100
        val results = (1 .. numRuns).map {
            RandomizedSSPayout().adjustPayout(stdPayout, earlyYear)
        }
        results.all { it == stdPayout }.shouldBeTrue()
    }

    afterTest {
        RandomizerFactory.setSuppressRandom(true)
    }
})
