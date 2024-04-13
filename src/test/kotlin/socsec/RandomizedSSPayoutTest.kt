package socsec

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import util.RandomizerFactory

class RandomizedSSPayoutTest : ShouldSpec({
    val stdPayout = 25000.0

    should("adjustPayout is always payout amount if supressRandom is one") {
        RandomizerFactory.setSuppressRandom(true)
        RandomizedSSPayout().adjustPayout(stdPayout).shouldBe(stdPayout)
    }

    should("once random value established, payout pct should always be the same") {
        RandomizerFactory.setSuppressRandom(false)
        val payoutProvider = RandomizedSSPayout()
        val initialPayout = payoutProvider.adjustPayout(stdPayout)

        repeat(1000) {
            payoutProvider.adjustPayout(initialPayout)
        }
    }

    should("should be 100% roughly 1/2 the time, 75% roughly 1/4 the time, and in between the rest") {
        RandomizerFactory.setSuppressRandom(false)
        val numRuns = 20000
        val results = (1 .. numRuns).map {
            RandomizedSSPayout().adjustPayout(stdPayout) / stdPayout
        }

        val pctFullPayout = results.filter{ it >= 1.00 }.size * 1.0 / results.size
        val pctLowPayout = results.filter { it <= 0.75 }.size * 1.0 / results.size

        pctFullPayout.shouldBeWithinPercentageOf(0.50, 5.0)
        pctLowPayout.shouldBeWithinPercentageOf(0.25, 10.0)
    }

    afterTest {
        RandomizerFactory.setSuppressRandom(true)
    }
})
