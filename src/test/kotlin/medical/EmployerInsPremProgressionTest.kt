package medical

import YearMonth
import config.EmployerInsurance
import config.employmentConfigFixture
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import util.DateRange
import util.currentDate
import yearlyDetailFixture

class EmployerInsPremProgressionTest : ShouldSpec({

    val year = currentDate.year + 1
    val empInsurance = EmployerInsurance(
        selfCost = 5000.0,
        spouseCost = 2500.0,
        dependantCost = 1000.0,
    )
    val empConfig = employmentConfigFixture(
        employerInsurance = empInsurance,
        dateRange = DateRange(end = YearMonth(year = year + 5))
    )

    val empInsurance2 = EmployerInsurance(
        selfCost = 4000.0,
        spouseCost = 2000.0,
        dependantCost = 1000.0,
    )
    val empConfig2 = employmentConfigFixture(
        employerInsurance = empInsurance2,
        dateRange = DateRange(
            start = YearMonth(year = year + 5))
    )

    val cmpdInflation = 1.2
    val inflation = inflationRecFixture(
        medRAC = InflationRAC(0.05, cmpdInflation, cmpdInflation + 0.05))
    val currYear = yearlyDetailFixture(year = year, inflation = inflation)

    should("determineNext returns inflation adjust premium based on relation when employerCoverage exist for all of current year ") {
        val config = empConfig
        val progSelf = EmployerInsPremProgression(listOf(config), RelationToInsured.SELF)
        val resultsSelf = progSelf.determineNext(currYear, previousAGI = 0.0 )
        resultsSelf.premium.shouldBe(empInsurance.selfCost * cmpdInflation)
        resultsSelf.monthsCovered.shouldBe(12)
        resultsSelf.fullyDeductAmount.shouldBe(resultsSelf.premium)
        resultsSelf.name.shouldBe(EmployerInsPremProgression.DESCRIPTION)

        val progSpouse = EmployerInsPremProgression(listOf(config), RelationToInsured.SPOUSE)
        val resultsSpouse = progSpouse.determineNext(currYear, previousAGI = 0.0 )
        resultsSpouse.premium.shouldBe(empInsurance.spouseCost * cmpdInflation)

        val progDepend = EmployerInsPremProgression(listOf(config), RelationToInsured.DEPENDANT)
        val resultsDepend = progDepend.determineNext(currYear, previousAGI = 0.0 )
        resultsDepend.premium.shouldBe(empInsurance.dependantCost * cmpdInflation)
    }

    should("determineNext returns prorated premium based when coverage exist for part of current year ") {
        val partialDateRange = empConfig.dateRange.copy(end = YearMonth(year, 6))
        val config = empConfig.copy(dateRange = partialDateRange)

        val prog = EmployerInsPremProgression(listOf(config), RelationToInsured.SELF)
        val results = prog.determineNext(currYear, previousAGI = 0.0 )
        results.premium.shouldBe(empInsurance.selfCost * cmpdInflation * .5)
        results.monthsCovered.shouldBe(6)
    }

    should("determineNext returns empty premium if coverage doesnt exist for year") {
        val partialDateRange = empConfig.dateRange.copy(end = YearMonth(year - 1))
        val config = empConfig.copy(dateRange = partialDateRange)

        val prog = EmployerInsPremProgression(listOf(config), RelationToInsured.SELF)
        val results = prog.determineNext(currYear, previousAGI = 0.0 )
        results.premium.shouldBe(0.0)
        results.monthsCovered.shouldBe(0)
    }

    should("determineNext returns empty premium if there is no employee insurance coverage") {
        val config = empConfig.copy(employerInsurance = null)

        val prog = EmployerInsPremProgression(listOf(config), RelationToInsured.SELF)
        val results = prog.determineNext(currYear, previousAGI = 0.0 )
        results.premium.shouldBe(0.0)
        results.monthsCovered.shouldBe(0)
    }

    should("determineNext returns premiums based employment on current year employment") {
        val configPresent = empConfig
        val configFuture = empConfig2

        val prog = EmployerInsPremProgression(
            listOf(configPresent, configFuture), RelationToInsured.SELF)

        val resultsPresent = prog.determineNext(currYear, previousAGI = 0.0 )
        resultsPresent.premium.shouldBe(empInsurance.selfCost * cmpdInflation)
        resultsPresent.monthsCovered.shouldBe(12)

        val futureYear = currYear.copy(year = configFuture.dateRange.start.year + 1)
        val resultsFuture = prog.determineNext(futureYear, previousAGI = 0.0 )
        resultsFuture.premium.shouldBe(empInsurance2.selfCost * cmpdInflation)
        resultsFuture.monthsCovered.shouldBe(12)
    }

    should("determineNext returns prorated premiums for each covereage when multiple employments in same year") {
        val firstEmpDateRange = empConfig.dateRange.copy(end = YearMonth(year, 3))
        val secondEmpDateRange = empConfig.dateRange.copy(start = YearMonth(year, 6))
        val configFirst = empConfig.copy(dateRange = firstEmpDateRange)
        val configSecond = empConfig2.copy(dateRange = secondEmpDateRange)

        val prog = EmployerInsPremProgression(
            listOf(configFirst, configSecond), RelationToInsured.SELF)

        val results = prog.determineNext(currYear, previousAGI = 0.0 )
        results.premium.shouldBe(
            empInsurance.selfCost * cmpdInflation * 0.25 +
                empInsurance2.selfCost * cmpdInflation * 0.5
        )
        results.monthsCovered.shouldBe(9)
    }
})
