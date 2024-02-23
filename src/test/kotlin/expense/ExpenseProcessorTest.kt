package expense

import config.configFixture
import config.householdConfigFixture
import config.householdMembersFixture
import config.parentConfigFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class ExpenseProcessorTest : ShouldSpec({
    val prevYear = yearlyDetailFixture()

    val householdName = "Household"
    val parent1Name = "Parent1"
    val parent2Name = "Parent2"

    val householdProgression1 = expenseCfgProgessFixture(
        name = "Household Exp1", person = householdName, amount = 1000.0)
    val householdProgression2 = expenseCfgProgessFixture(
        name = "Household Exp2", person = householdName, amount = 2000.0)
    val parent1Progression = expenseCfgProgessFixture(
        name = "Parent 1 Exp", person = parent1Name, amount = 3000.0)
    val parent2Progression = expenseCfgProgessFixture(
        name = "Parent 2 Exp", person = parent2Name, amount = 4000.0)

    val parent1 = parentConfigFixture(
        name = "Parent 1", expenseConfigs = listOf(parent1Progression))
    val parent2 = parentConfigFixture(
        name = "Parent 2", expenseConfigs = listOf(parent2Progression))
    val householdConfig = householdConfigFixture(
        householdMembers = householdMembersFixture(parent1, parent2),
        expenses = listOf(householdProgression1, householdProgression2)
    )
    val config = configFixture(householdConfig = householdConfig)

    should("process all household and household member expenses for the year") {
        val result: List<ExpenseRec> = ExpenseProcessor.process(config, prevYear)

        result.size.shouldBe(4)

        result.find {
            it.config.person == householdName &&
                it.config.name == householdProgression1.config.name
        }.shouldNotBeNull().amount.shouldBe(1000.0)

        result.find {
            it.config.person == householdName &&
                it.config.name == householdProgression2.config.name

        }.shouldNotBeNull().amount.shouldBe(2000.0)

        result.find {
            it.config.person == parent1Name &&
                it.config.name == parent1Progression.config.name
        }.shouldNotBeNull().amount.shouldBe(3000.0)

        result.find {
            it.config.person == parent2Name &&
                it.config.name == parent2Progression.config.name
        }.shouldNotBeNull().amount.shouldBe(4000.0)
    }
})

