package medical

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class DependantInsFixedYearProgressionTest : ShouldSpec({
    val startYear = currentDate.year + 1
    val progression = DependantInsFixedYearProgression(startYear = startYear)

    should("determineNext returns months coverage based on whether start year of dependant of having own insurance") {
        val yearBeforeStart = yearlyDetailFixture(year = startYear -1)
        val beforeYearResult = progression.determineNext(yearBeforeStart, previousAGI = 0.0)
        beforeYearResult.monthsCovered.shouldBe(0)
        beforeYearResult.premium.shouldBe(0.0)
        beforeYearResult.name.shouldBe(DependantInsFixedYearProgression.DESCRIPTION)

        val yearAfterStart = yearlyDetailFixture(year = startYear +1)
        val afterYearResult = progression.determineNext(yearAfterStart, previousAGI = 0.0)
        afterYearResult.monthsCovered.shouldBe(12)
        afterYearResult.premium.shouldBe(0.0)
        afterYearResult.name.shouldBe(DependantInsFixedYearProgression.DESCRIPTION)

        val yearOfStart = yearlyDetailFixture(year = startYear)
        val yearOfResult = progression.determineNext(yearOfStart, previousAGI = 0.0 )
        yearOfResult.monthsCovered.shouldBe(12)
        yearOfResult.premium.shouldBe(0.0)
        yearOfResult.name.shouldBe(DependantInsFixedYearProgression.DESCRIPTION)
    }
})
