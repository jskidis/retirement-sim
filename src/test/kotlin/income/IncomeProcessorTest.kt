package income

import config.configFixture
import config.householdConfigFixture
import config.householdMembersFixture
import config.parentConfigFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class IncomeProcessorTest : ShouldSpec({
    val prevYear = yearlyDetailFixture()

    val parent1Name = "Parent1"
    val parent2Name = "Parent2"

    val parent1Progression = incomeCfgProgessFixture(
        name = "Parent 1 Inc", person = parent1Name, amount = 3000.0)
    val parent2Progression = incomeCfgProgessFixture(
        name = "Parent 2 Inc", person = parent2Name, amount = 4000.0)

    val parent1 = parentConfigFixture(
        name = "Parent 1", incomeConfigs = listOf(parent1Progression))
    val parent2 = parentConfigFixture(
        name = "Parent 2", incomeConfigs = listOf(parent2Progression))
    val householdConfig = householdConfigFixture(householdMembersFixture(parent1, parent2))
    val config = configFixture(householdConfig = householdConfig)

    should("process all household and household member expenses for the year") {
        val result: List<IncomeRec> = IncomeProcessor.process(config, prevYear)

        result.size.shouldBe(2)

        result.find {
            it.config.name == "Parent 1 Inc" &&
                it.config.person == parent1Name && it.amount == 3000.0
        }.shouldNotBeNull()

        result.find {
            it.config.name == "Parent 2 Inc" &&
                it.config.person == parent2Name && it.amount == 4000.0
        }.shouldNotBeNull()
    }
})


