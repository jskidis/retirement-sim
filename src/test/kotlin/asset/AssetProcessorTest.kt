package asset

import config.configFixture
import config.householdConfigFixture
import config.householdMembersFixture
import config.parentConfigFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class AssetProcessorTest : ShouldSpec({
    val prevYear = yearlyDetailFixture()

    val householdName = "Household"
    val parent1Name = "Parent 1"
    val parent2Name = "Parent 2"

    val householdProgression1 = assetConfigProgressFixture(
        name = "Joint Asset 1", person = householdName,
        startBal = 10000.0, gains = 1000.0)
    val parent1Progression = assetConfigProgressFixture(
        name = "Parent 1 Asset", person = parent1Name,
        startBal = 30000.0, gains = 3000.0)
    val parent2Progression = assetConfigProgressFixture(
        name = "Parent 2 Asset", person = parent2Name,
        startBal = 40000.0, gains = 4000.0)

    val parent1 = parentConfigFixture(
        name = "Parent 1", assetConfigs = listOf(parent1Progression))
    val parent2 = parentConfigFixture(
        name = "Parent 2", assetConfigs = listOf(parent2Progression))
    val householdConfig = householdConfigFixture(
        householdMembers = householdMembersFixture(parent1, parent2),
        jointAssets = listOf(householdProgression1)
    )
    val config = configFixture(householdConfig = householdConfig)

    should("process all household and household member assets for the year") {
        val result: List<AssetRec> = AssetProcessor.process(config, prevYear)

        result.shouldHaveSize(3)

        val jointAsset = result.find {
            it.config.person == householdName &&
                it.config.name == householdProgression1.config.name
        }
        jointAsset.shouldNotBeNull()
        jointAsset.gains.amount.shouldBe(1000.0)

        val parent1Asset = result.find {
            it.config.person == parent1Name &&
                it.config.name == parent1Progression.config.name
        }
        parent1Asset.shouldNotBeNull()
        parent1Asset.gains.amount.shouldBe(3000.0)

        val parent2Asset = result.find {
            it.config.person == parent2Name &&
                it.config.name == parent2Progression.config.name
        }
        parent2Asset.shouldNotBeNull()
        parent2Asset.gains.amount.shouldBe(4000.0)
    }
})

