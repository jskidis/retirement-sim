package income

import config.configFixture
import config.householdConfigFixture
import config.householdMembersFixture
import config.parentConfigFixture
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeWithinPercentageOf
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.SS_INCOME_CAP
import yearlyDetailFixture

class IncomeProcessorTest : ShouldSpec({
    val parent1Name = "Parent1"
    val parent2Name = "Parent2"

    val parent1Income = ConstantsProvider.getValue(SS_INCOME_CAP) + 1000
    val parent2Income = ConstantsProvider.getValue(SS_INCOME_CAP) * 2.0

    val parent1Progression = incomeProgressionFixture(
        name = "Parent 1 Inc", person = parent1Name, amount = parent1Income)
    val parent2Progression = incomeProgressionFixture(
        name = "Parent 2 Inc", person = parent2Name, amount = parent2Income)

    val parent1 = parentConfigFixture(
        name = "Parent 1", incomeConfigs = listOf(parent1Progression))
    val parent2 = parentConfigFixture(
        name = "Parent 2", incomeConfigs = listOf(parent2Progression))
    val householdConfig = householdConfigFixture(householdMembersFixture(parent1, parent2))
    val config = configFixture(householdConfig = householdConfig)

    should("process all household and household member expenses for the year") {
        val prevYear = yearlyDetailFixture()
        val result: List<IncomeRec> = IncomeProcessor.process(config, prevYear)

        result.shouldHaveSize(2)

        result.find {
            it.ident.name == "Parent 1 Inc" &&
                it.ident.person == parent1Name && it.amount() == parent1Income
        }.shouldNotBeNull()

        result.find {
            it.ident.name == "Parent 2 Inc" &&
                it.ident.person == parent2Name && it.amount() == parent2Income
        }.shouldNotBeNull()
    }

    should("cap individual soc security wages") {
        val result: List<IncomeRec> = IncomeProcessor.process(config, null)

        val parent1Rec = result.find {
            it.ident.name == "Parent 1 Inc" &&
                it.ident.person == parent1Name && it.amount() == parent1Income
        }
        parent1Rec.shouldNotBeNull()
        // salary above cap
        parent1Rec.taxableIncome.socSec.shouldBeWithinPercentageOf(
            ConstantsProvider.getValue(SS_INCOME_CAP), .001)

        val parent2Rec = result.find {
            it.ident.name == "Parent 2 Inc" &&
                it.ident.person == parent2Name && it.amount() == parent2Income
        }
        parent2Rec.shouldNotBeNull()
        // salary above cap
        parent2Rec.taxableIncome.socSec.shouldBeWithinPercentageOf(
            ConstantsProvider.getValue(SS_INCOME_CAP), .001)
    }

    should("cap individual soc security wages, cap is adjust for inflation") {
        val cmpWageInflation = 1.50
        inflationRecFixture(wageRAC = InflationRAC(rate = 0.03, cmpWageInflation))
        val prevYear = yearlyDetailFixture(
            inflation = inflationRecFixture(
                wageRAC = InflationRAC(0.03, 1.47, 1.50))
        )
        val result: List<IncomeRec> = IncomeProcessor.process(config, prevYear)

        val parent1Rec = result.find {
            it.ident.name == "Parent 1 Inc" &&
                it.ident.person == parent1Name && it.amount() == parent1Income
        }
        parent1Rec.shouldNotBeNull()
        // salary below inflation adjusted cap
        parent1Rec.taxableIncome.socSec.shouldBe(parent1Income)

        val parent2Rec = result.find {
            it.ident.name == "Parent 2 Inc" &&
                it.ident.person == parent2Name && it.amount() == parent2Income
        }
        parent2Rec.shouldNotBeNull()
        // salary above inflation adjusted cap
        parent2Rec.taxableIncome.socSec.shouldBeWithinPercentageOf(
            ConstantsProvider.getValue(SS_INCOME_CAP) * cmpWageInflation, .001)

    }
})


