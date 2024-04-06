package expense

import config.configFixture
import config.householdConfigFixture
import config.householdMembersFixture
import config.parentConfigFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class ExpenseProcessorTest : ShouldSpec({
    val prevYear = yearlyDetailFixture()

    val householdName = "Household"
    val parent1Name = "Parent1"
    val parent2Name = "Parent2"

    val householdProgression1 = expenseProgressionFixture(
        name = "Household Exp1", person = householdName, amount = 1000.0)
    val householdProgression2 = expenseProgressionFixture(
        name = "Household Exp2", person = householdName, amount = 2000.0)
    val parent1Progression = expenseProgressionFixture(
        name = "Parent 1 Exp", person = parent1Name, amount = 3000.0)
    val parent2Progression = expenseProgressionFixture(
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

        result.shouldHaveSize(4)

        result.find {
            it.ident.person == householdName &&
                it.ident.name == "Household Exp1"
        }.shouldNotBeNull().amount.shouldBe(1000.0)

        result.find {
            it.ident.person == householdName &&
                it.ident.name == "Household Exp2"

        }.shouldNotBeNull().amount.shouldBe(2000.0)

        result.find {
            it.ident.person == parent1Name &&
                it.ident.name == "Parent 1 Exp"
        }.shouldNotBeNull().amount.shouldBe(3000.0)

        result.find {
            it.ident.person == parent2Name &&
                it.ident.name == "Parent 2 Exp"
        }.shouldNotBeNull().amount.shouldBe(4000.0)
    }
})

