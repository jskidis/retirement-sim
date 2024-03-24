package medical

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class FixedYearDependentInsProgressionTest : FunSpec({
    val startYear = 2030
    val progression = FixedYearDependentInsProgression(startYear = startYear)

    test("determineNext returns months coverage based on whether start year of dependant of having own insurance") {
        val yearBeforeStart = yearlyDetailFixture(year = startYear -1)
        val beforeYearResult = progression.determineNext(yearBeforeStart)
        beforeYearResult.monthsCovered.shouldBe(0)
        beforeYearResult.premium.shouldBe(0.0)

        val yearAfterStart = yearlyDetailFixture(year = startYear +1)
        val afterYearResult = progression.determineNext(yearAfterStart)
        afterYearResult.monthsCovered.shouldBe(12)
        afterYearResult.premium.shouldBe(0.0)

        val yearOfStart = yearlyDetailFixture(year = startYear)
        val yearOfResult = progression.determineNext(yearOfStart)
        yearOfResult.monthsCovered.shouldBe(12)
        yearOfResult.premium.shouldBe(0.0)
    }
})
