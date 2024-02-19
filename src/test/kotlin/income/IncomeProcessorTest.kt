package income

import config.configFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class IncomeProcessorTest : ShouldSpec({
    val config = configFixture()
    val prevYear = yearlyDetailFixture()

    val parent1Name = config.householdMembers.parent1.name
    val parent2Name = config.householdMembers.parent2.name

    val parent1Progression = incomeCfgProgessFixture(
        name = "Parent 1 Inc", person = parent1Name, amount = 3000.0)
    val parent2Progression = incomeCfgProgessFixture(
        name = "Parent 2 Inc", person = parent2Name, amount = 4000.0)

    config.householdMembers.parent1.otherIncomes = listOf(parent1Progression)
    config.householdMembers.parent2.otherIncomes = listOf(parent2Progression)

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


