package medical

import Amount
import YearlyDetail
import config.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import util.currentDate
import yearlyDetailFixture

class MedInsuranceProcessorTest : FunSpec({

    val memberConfig = householdMembersFixture()
    val householdConfig = householdConfigFixture()
    val baseConfig = configFixture()

    val year = currentDate.year + 1
    val currentYear = yearlyDetailFixture(year = year)

    val personName = "Person1"
    val personName2 = "Person2"
    fun buildConfig(
        medInsurance1: List<MedInsuranceProgression>,
        medInsurance2: List<MedInsuranceProgression> = ArrayList(),
    ): SimConfig =
        baseConfig.copy(
            household =
            householdConfig.copy(
                members =
                memberConfig.copy(
                    parent1 = parentConfigFixture(
                        name = personName, medInsuranceConfigs = medInsurance1),
                    parent2 = parentConfigFixture(
                        name = personName2, medInsuranceConfigs = medInsurance2)
                )
            )
        )

    val fullYearCoverage = InsurancePrem(
        name = "Full", premium = 1000.0, monthsCovered = 12, fullyDeductAmount = 500.0)

    val noCoverage = InsurancePrem(name = "None")

    val quarterYearCoverage = InsurancePrem(
        name = "Quarter", premium = 500.0, monthsCovered = 3)


    test("processes med insurance cost, single full year coverage") {
        val progression = MedInsuranceProgressionFixture(fullYearCoverage)
        val config = buildConfig(listOf(progression))

        val result = MedInsuranceProcessor.process(config, currentYear, previousAGI = 0.0)
        result.shouldHaveSize(1)
        result[0].year.shouldBe(year)
        result[0].config.name.shouldBe(fullYearCoverage.name)
        result[0].config.person.shouldBe(personName)
        result[0].amount.shouldBe(fullYearCoverage.premium)
        result[0].taxDeductions.person.shouldBe(personName)
        result[0].taxDeductions.fed.shouldBe(fullYearCoverage.fullyDeductAmount)
        result[0].taxDeductions.state.shouldBe(fullYearCoverage.fullyDeductAmount)
    }

    test("processes med insurance cost, multiple options only one is current") {
        val progression1 = MedInsuranceProgressionFixture(noCoverage)
        val progression2 = MedInsuranceProgressionFixture(fullYearCoverage)
        val config = buildConfig(listOf(progression1, progression2))

        val result = MedInsuranceProcessor.process(config, currentYear, previousAGI = 0.0)
        result.shouldHaveSize(1)
        result[0].year.shouldBe(year)
        result[0].config.name.shouldBe(fullYearCoverage.name)
        result[0].config.person.shouldBe(personName)
        result[0].amount.shouldBe(fullYearCoverage.premium)
        result[0].taxDeductions.person.shouldBe(personName)
        result[0].taxDeductions.fed.shouldBe(fullYearCoverage.fullyDeductAmount)
        result[0].taxDeductions.state.shouldBe(fullYearCoverage.fullyDeductAmount)
    }

    test("process med insurance, no options current") {
        val progression1 = MedInsuranceProgressionFixture(noCoverage)
        val config = buildConfig(listOf(progression1))

        val result = MedInsuranceProcessor.process(config, currentYear, previousAGI = 0.0)
        result.shouldHaveSize(0)
    }

    test("processes med insurance cost, multiple options both valid, first full year") {
        val progression1 = MedInsuranceProgressionFixture(fullYearCoverage)
        val progression2 = MedInsuranceProgressionFixture(quarterYearCoverage)
        val config = buildConfig(listOf(progression1, progression2))

        val result = MedInsuranceProcessor.process(config, currentYear, previousAGI = 0.0)
        result.shouldHaveSize(1)
        result[0].year.shouldBe(year)
        result[0].config.name.shouldBe(fullYearCoverage.name)
        result[0].config.person.shouldBe(personName)
        result[0].amount.shouldBe(fullYearCoverage.premium)
        result[0].taxDeductions.person.shouldBe(personName)
        result[0].taxDeductions.fed.shouldBe(fullYearCoverage.fullyDeductAmount)
        result[0].taxDeductions.state.shouldBe(fullYearCoverage.fullyDeductAmount)
    }

    test("processes med insurance cost, multiple options both valid, first only partial year") {
        val progression1 = MedInsuranceProgressionFixture(quarterYearCoverage)
        val progression2 = MedInsuranceProgressionFixture(fullYearCoverage)
        val config = buildConfig(listOf(progression1, progression2))

        val result = MedInsuranceProcessor.process(config, currentYear, previousAGI = 0.0)
        result.shouldHaveSize(2)

        result[0].year.shouldBe(year)
        result[0].config.name.shouldBe(quarterYearCoverage.name)
        result[0].config.person.shouldBe(personName)
        result[0].amount.shouldBe(quarterYearCoverage.premium)
        result[0].taxDeductions.person.shouldBe(personName)
        result[0].taxDeductions.fed.shouldBe(quarterYearCoverage.fullyDeductAmount)
        result[0].taxDeductions.state.shouldBe(quarterYearCoverage.fullyDeductAmount)

        result[1].year.shouldBe(year)
        result[1].config.name.shouldBe(fullYearCoverage.name)
        result[1].config.person.shouldBe(personName)
        result[1].amount.shouldBe(fullYearCoverage.premium * 3 / 4)
        result[1].taxDeductions.person.shouldBe(personName)
        result[1].taxDeductions.fed.shouldBe(fullYearCoverage.fullyDeductAmount * 3 / 4)
        result[1].taxDeductions.state.shouldBe(fullYearCoverage.fullyDeductAmount * 3 / 4)
    }

    test("processes med insurance cost for multiple people") {
        val progression1 = MedInsuranceProgressionFixture(quarterYearCoverage)
        val progression2 = MedInsuranceProgressionFixture(fullYearCoverage)
        val progression3 = MedInsuranceProgressionFixture(fullYearCoverage)
        val config = buildConfig(
            listOf(progression1, progression2),
            listOf(progression3)
        )

        val result = MedInsuranceProcessor.process(config, currentYear, previousAGI = 0.0)
        result.shouldHaveSize(3)

        result[0].year.shouldBe(year)
        result[0].config.name.shouldBe(quarterYearCoverage.name)
        result[0].config.person.shouldBe(personName)
        result[0].amount.shouldBe(quarterYearCoverage.premium)
        result[0].taxDeductions.person.shouldBe(personName)
        result[0].taxDeductions.fed.shouldBe(quarterYearCoverage.fullyDeductAmount)
        result[0].taxDeductions.state.shouldBe(quarterYearCoverage.fullyDeductAmount)

        result[1].year.shouldBe(year)
        result[1].config.name.shouldBe(fullYearCoverage.name)
        result[1].config.person.shouldBe(personName)
        result[1].amount.shouldBe(fullYearCoverage.premium * 3 / 4)
        result[1].taxDeductions.person.shouldBe(personName)
        result[1].taxDeductions.fed.shouldBe(fullYearCoverage.fullyDeductAmount * 3 / 4)
        result[1].taxDeductions.state.shouldBe(fullYearCoverage.fullyDeductAmount * 3 / 4)

        result[2].year.shouldBe(year)
        result[2].config.name.shouldBe(fullYearCoverage.name)
        result[2].config.person.shouldBe(personName2)
        result[2].amount.shouldBe(fullYearCoverage.premium)
        result[2].taxDeductions.person.shouldBe(personName2)
        result[2].taxDeductions.fed.shouldBe(fullYearCoverage.fullyDeductAmount)
        result[2].taxDeductions.state.shouldBe(fullYearCoverage.fullyDeductAmount)
    }
})

class MedInsuranceProgressionFixture(val insurancePrem: InsurancePrem) : MedInsuranceProgression {
    override fun determineNext(currYear: YearlyDetail, previousAGI: Amount): InsurancePrem = insurancePrem
}
