package medical

import YearMonth
import config.ConfigConstants
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class MedicareProgressionTest : FunSpec({

    val year = 2020
    val cmpdInflation = 1.2
    val inflation = inflationRecFixture(
        medRAC = InflationRAC(0.05, cmpdInflation, cmpdInflation + 0.05))

    val person66YO = YearMonth(year = year - 66, month = 0)
    val person64YO = YearMonth(year = year - 64, month = 0)
    val person65YO = YearMonth(year = year - 65, month = 5)

    val currYear = yearlyDetailFixture(year = year, inflation = inflation)

    test("determineNext returns full prem (adjusted for inflation) with 12 months covered if person is 66+ YO") {
        val progression = MedicareProgression(person66YO)
        val results = progression.determineNext(currYear)
        results.premium.shouldBe(ConfigConstants.baseMedicarePrem * inflation.med.cmpdStart)
        results.monthsCovered.shouldBe(12)
        results.fullyDeductAmount.shouldBe(0.0)
        results.name.shouldBe(MedicareProgression.DESCRIPTION)
    }

    test("determineNext return 'empty' premium object is person is less an 65") {
        val progression = MedicareProgression(person64YO)
        val results = progression.determineNext(currYear)
        results.premium.shouldBe(0.0)
        results.monthsCovered.shouldBe(0)
    }

    test("determineNext returns partial prem (adjusted for inflation) with partial months covered if person turns 65 in current year") {
        val progression = MedicareProgression(person65YO)
        val results = progression.determineNext(currYear)
        results.premium.shouldBe(
            ConfigConstants.baseMedicarePrem *
                inflation.med.cmpdStart *
                (11 - person65YO.month) / 12.0)
        results.monthsCovered.shouldBe(11 - person65YO.month)
        // assumes coverage won't start until end of month person turns 65
    }
})
