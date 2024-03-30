package medical

import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import tax.FilingStatus
import util.currentDate
import yearlyDetailFixture

class MedicarePremCalcTest : FunSpec({
    val year = currentDate.year + 5
    val brackets = listOf(
        MedicarePremBracketRec(
            singleBracket = MedicareBracket(start = 0.0, end = 10000.0),
            jointBracket = MedicareBracket(start = 0.0, end = 20000.0),
            partPrems = MedicarePartPrems(
                partBPrem = 2000.0, partDPrem = 500.0,
                medigap = 1000.0, dental = 600.0)
        ),
        MedicarePremBracketRec(
            singleBracket = MedicareBracket(start = 10000.0, end = 20000.0),
            jointBracket = MedicareBracket(start = 20000.0, end = 40000.0),
            partPrems = MedicarePartPrems(
                partBPrem = 2200.0, partDPrem = 600.0,
                medigap = 1000.0, dental = 600.0)
        ),
        MedicarePremBracketRec(
            singleBracket = MedicareBracket(start = 20000.0, end = Double.MAX_VALUE),
            jointBracket = MedicareBracket(start = 40000.0, end = Double.MAX_VALUE),
            partPrems = MedicarePartPrems(
                partBPrem = 2400.0, partDPrem = 700.0,
                medigap = 1000.0, dental = 600.0)
        ),
    )

    val calculator = MedicarePremCalcFixture(brackets)

    val cmpdStdInfl = 1.5
    val cmpdMedInfl = 2.0
    val inflation = inflationRecFixture(
        stdRAC = InflationRAC(.03, cmpdStdInfl, cmpdStdInfl + .03),
        medRAC = InflationRAC(.05, cmpdMedInfl, cmpdMedInfl + .05))

    test("getPremium calculates premium when filing method is single and agi (adjusted for inflation) is in first bracket") {
        val agi = (brackets[0].singleBracket.end - 1.0) * cmpdStdInfl
        val currYear = yearlyDetailFixture(year, inflation, filingStatus = FilingStatus.SINGLE)
        val parts = listOf(MedicarePartType.PARTB, MedicarePartType.PARTD)
        val results = calculator.getMedicarePremium(currYear, agi, parts)
        results.shouldBe(
            brackets[0].partPrems.partBPrem * cmpdMedInfl +
                brackets[0].partPrems.partDPrem * cmpdMedInfl)
    }

    test("getPremium calculates premium when filing method is head of household and agi (adjusted for inflation) is in first bracket") {
        val agi = (brackets[0].singleBracket.end - 1.0) * cmpdStdInfl
        val currYear = yearlyDetailFixture(year, inflation, filingStatus = FilingStatus.HOUSEHOLD)
        val parts = listOf(MedicarePartType.PARTB, MedicarePartType.PARTD)
        val results = calculator.getMedicarePremium(currYear, agi, parts)
        results.shouldBe(
            brackets[0].partPrems.partBPrem * cmpdMedInfl +
                brackets[0].partPrems.partDPrem * cmpdMedInfl)
    }

    test("getPremium calculates premium when filing method is jointly and agi (adjusted for inflation) is in first bracket") {
        val agi = (brackets[0].jointBracket.end - 1.0) * cmpdStdInfl
        val currYear = yearlyDetailFixture(year, inflation, filingStatus = FilingStatus.JOINTLY)
        val parts = listOf(MedicarePartType.PARTB, MedicarePartType.PARTD)
        val results = calculator.getMedicarePremium(currYear, agi, parts)
        results.shouldBe(
            brackets[0].partPrems.partBPrem * cmpdMedInfl +
                brackets[0].partPrems.partDPrem * cmpdMedInfl)
    }

    test("getPremium calculates premium when filing method is single and agi (adjusted for inflation) is in second bracket") {
        val agi = (brackets[1].singleBracket.end - 1.0) * cmpdStdInfl
        val currYear = yearlyDetailFixture(year, inflation, filingStatus = FilingStatus.SINGLE)
        val parts = listOf(MedicarePartType.PARTB, MedicarePartType.PARTD)
        val results = calculator.getMedicarePremium(currYear, agi, parts)
        results.shouldBe(
            brackets[1].partPrems.partBPrem * cmpdMedInfl +
                brackets[1].partPrems.partDPrem * cmpdMedInfl)
    }

    test("getPremium calculates premium when filing method is jointly and agi is in the last bracket") {
        val agi = 5000000.0
        val currYear = yearlyDetailFixture(year, inflation, filingStatus = FilingStatus.JOINTLY)
        val parts = listOf(MedicarePartType.PARTB, MedicarePartType.PARTD)
        val results = calculator.getMedicarePremium(currYear, agi, parts)
        results.shouldBe(
            brackets[2].partPrems.partBPrem * cmpdMedInfl +
                brackets[2].partPrems.partDPrem * cmpdMedInfl)
    }

    test("getPremium calculates premium using all the parts ") {
        val agi = (brackets[0].singleBracket.end - 1.0) * cmpdStdInfl
        val currYear = yearlyDetailFixture(year, inflation, filingStatus = FilingStatus.SINGLE)
        val parts = listOf(MedicarePartType.PARTB, MedicarePartType.PARTD,
            MedicarePartType.MEDIGAP, MedicarePartType.DENTAL)
        val results = calculator.getMedicarePremium(currYear, agi, parts)

        results.shouldBe(cmpdMedInfl *
            (brackets[0].partPrems.partBPrem + brackets[0].partPrems.partDPrem +
                brackets[0].partPrems.medigap + brackets[0].partPrems.dental)
        )
    }
})

class MedicarePremCalcFixture(val b: List<MedicarePremBracketRec>) : MedicarePremCalc() {
    override fun getBrackets(): List<MedicarePremBracketRec> = b
}
