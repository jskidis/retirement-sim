package asset

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe

class RmdPctTest : ShouldSpec({

    should("getRmdPct reads from rmd table and returns pct") {
        RmdPct.getRmdPct(60).shouldBe(0.0)
        RmdPct.getRmdPct(75).shouldBeGreaterThan(0.0).shouldBeLessThan(0.25)
    }
})
