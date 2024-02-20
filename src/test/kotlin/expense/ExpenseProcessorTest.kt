package expense

import config.configFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class ExpenseProcessorTest : ShouldSpec({
    val config = configFixture()
    val prevYear = yearlyDetailFixture()

    val householdName = "Household"
    val parent1Name = config.householdMembers.parent1.name
    val parent2Name = config.householdMembers.parent2.name

    val householdProgression1 = expenseCfgProgessFixture(
        name = "Household Exp1", person = householdName, amount = 1000.0)
    val householdProgression2 = expenseCfgProgessFixture(
        name = "Household Exp2", person = householdName, amount = 2000.0)
    val parent1Progression = expenseCfgProgessFixture(
        name = "Parent 1 Exp", person = parent1Name, amount = 3000.0)
    val parent2Progression = expenseCfgProgessFixture(
        name = "Parent 2 Exp", person = parent2Name, amount = 4000.0)

    config.householdExpenses = listOf(householdProgression1, householdProgression2)
    config.householdMembers.parent1.expenses = listOf(parent1Progression)
    config.householdMembers.parent2.expenses = listOf(parent2Progression)

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

