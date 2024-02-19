package inflation

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe

class RateAndCompoundTest : ShouldSpec({
    should("build returns rate as specified rate and compound of (1 + rate) + previous compound") {
        var result = InflationRAC.build(currRate = 0.1,
            prev = InflationRAC(rate = 0.1, cmpdStart = 1.0, cmpdEnd = 1.1))

        result.rate.shouldBe(0.1)
        result.cmpdStart.shouldBeWithinPercentageOf(1.0 * (1 + 0.1), percentage = .001)
        result.cmpdEnd.shouldBeWithinPercentageOf(1.1 * (1 + 0.1), percentage = .001)

        result = InflationRAC.build(currRate = 0.2,
            prev = InflationRAC(rate = 0.1, cmpdStart = 1.1, cmpdEnd = 1.21))
        result.rate.shouldBe(0.2)
        result.cmpdStart.shouldBeWithinPercentageOf(1.1 * (1 + 0.2), .001)
        result.cmpdEnd.shouldBeWithinPercentageOf(1.21 * (1 + 0.2), percentage = .001)
    }

})