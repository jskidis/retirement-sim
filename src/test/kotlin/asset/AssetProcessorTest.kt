package asset

import assetCfgProgessFixture
import config.configFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import yearlyDetailFixture

class AssetProcessorTest : ShouldSpec({
    val config = configFixture()
    val prevYear = yearlyDetailFixture()

    val householdName = "Household"
    val parent1Name = config.householdMembers.parent1.name
    val parent2Name = config.householdMembers.parent2.name

    val householdProgression1 = assetCfgProgessFixture(
        name = "Joint Asset 1", person = householdName,
        startBal = 10000.0, gains = listOf(1000.0, 2000.0))
    val parent1Progression = assetCfgProgessFixture(
        name = "Parent 1 Asset", person = parent1Name,
        startBal = 30000.0, gains = listOf(3000.0))
    val parent2Progression = assetCfgProgessFixture(
        name = "Parent 2 Asset", person = parent2Name,
        startBal = 40000.0, gains = listOf(4000.0))

    config.jointAssets = listOf(householdProgression1)
    config.householdMembers.parent1.assets = listOf(parent1Progression)
    config.householdMembers.parent2.assets = listOf(parent2Progression)

    should("process all household and household member assets for the year") {
        val result: List<AssetRec> = AssetProcessor.process(config, prevYear)

        result.size.shouldBe(3)

        val jointAsset = result.find {
            it.config.person == householdName &&
                it.config.name == householdProgression1.config.name
        }
        jointAsset.shouldNotBeNull()
        jointAsset.gains.size.shouldBe(2)
        jointAsset.gains[0].amount.shouldBe(1000.0)
        jointAsset.gains[1].amount.shouldBe(2000.0)

        val parent1Asset = result.find {
            it.config.person == parent1Name &&
                it.config.name == parent1Progression.config.name
        }
        parent1Asset.shouldNotBeNull()
        parent1Asset.gains.size.shouldBe(1)
        parent1Asset.gains[0].amount.shouldBe(3000.0)

        val parent2Asset = result.find {
            it.config.person == parent2Name &&
                it.config.name == parent2Progression.config.name
        }
        parent2Asset.shouldNotBeNull()
        parent2Asset.gains.size.shouldBe(1)
        parent2Asset.gains[0].amount.shouldBe(4000.0)
    }
})

