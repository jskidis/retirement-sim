package medical

import YearMonth
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class MedicareProgressionTest : ShouldSpec({

    val year = 2020
    val cmpdInflation = 1.2
    val inflation = inflationRecFixture(
        medRAC = InflationRAC(0.05, cmpdInflation, cmpdInflation + 0.05))

    val person66YO = YearMonth(year = year - 66, month = 0)
    val person64YO = YearMonth(year = year - 64, month = 0)
    val person65YO = YearMonth(year = year - 65, month = 5)

    val premium = 2500.0
    val parts = listOf(MedicarePartType.PARTB, MedicarePartType.PARTD)

    val currYear = yearlyDetailFixture(year = year, inflation = inflation)

    should("determineNext returns full prem (adjusted for inflation) with 12 months covered if person is 66+ YO") {
        val progression = MedicareProgressionFixture(person66YO, parts, premium)
        val results = progression.determineNext(currYear, previousAGI = 0.0)
        results.premium.shouldBe(premium)
        results.monthsCovered.shouldBe(12)
        results.fullyDeductAmount.shouldBe(0.0)
        results.name.shouldBe(MedicareProgression.DESCRIPTION)
    }

    should("determineNext return 'empty' premium object is person is less an 65") {
        val progression = MedicareProgressionFixture(person64YO, parts, premium)
        val results = progression.determineNext(currYear, previousAGI = 0.0)
        results.premium.shouldBe(0.0)
        results.monthsCovered.shouldBe(0)
    }

    should("determineNext returns partial prem (adjusted for inflation) with partial months covered if person turns 65 in current year") {
        val progression = MedicareProgressionFixture(person65YO, parts, premium)
        val results = progression.determineNext(currYear, previousAGI = 0.0)
        results.premium.shouldBe(
            premium * (11 - person65YO.month) / 12.0)
        results.monthsCovered.shouldBe(11 - person65YO.month)
        // assumes coverage won't start until end of month person turns 65
    }
})

class MedicareProgressionFixture(
    birthYM: YearMonth,
    parts: List<MedicarePartType>,
    premium: Double,
) : MedicareProgression(
    birthYM = birthYM,
    parts = parts,
    medicarePremProvider = MedicarePremProvider { _, _, _ -> premium }
)