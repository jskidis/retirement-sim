package cashflow

import RecIdentifier
import asset.AssetChange
import asset.AssetRec
import asset.AssetType
import config.configFixture
import config.householdConfigFixture
import config.personConfigFixture
import config.personFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainAll
import util.currentDate
import yearlyDetailFixture

class CashFlowEventProcessorTest : ShouldSpec({

    val year = currentDate.year + 1

    val ident1 = RecIdentifier(name = "Asset 1", person = "Person")
    val ident2 = RecIdentifier(name = "Asset 2", person = "Person")
    val ident3 = RecIdentifier(name = "Asset 3", person = "Person")
    val ident4 = RecIdentifier(name = "Asset 4", person = "Person")
    val ident5 = RecIdentifier(name = "Asset 5", person = "Person")

    val gains = AssetChange("Gains", 0.0)
    val rec1 = AssetRec(year, ident1, AssetType.IRA, 0.0, 0.0, gains)
    val rec2 = AssetRec(year, ident2, AssetType.ROTH, 0.0, 0.0, gains)
    val rec3 = AssetRec(year, ident3, AssetType.STD401K, 0.0, 0.0, gains)
    val rec4 = AssetRec(year, ident4, AssetType.ROTH401K, 0.0, 0.0, gains)
    val recs = listOf(rec1, rec2, rec3, rec4)

    val change1 = AssetChange("CFE1", 2000.0, null)
    val change2: AssetChange? = null
    val change4_1 = AssetChange("CFE4-1", -3000.0, null)
    val change4_2 = AssetChange("CFE4-2", 4000.0, null)
    val change5 = AssetChange("CFE5", 1000.0, null)

    val handler1 = CashFlowEventHandler { _, _ -> change1 }
    val handler2 = CashFlowEventHandler { _, _ -> change2 }
    val handler4_1 = CashFlowEventHandler { _, _ -> change4_1 }
    val handler4_2 = CashFlowEventHandler { _, _ -> change4_2 }
    val handler5 = CashFlowEventHandler { _, _ -> change5 }

    val eventConfigs = listOf(
        CashFlowEventConfig(ident1, handler1),
        CashFlowEventConfig(ident2, handler2),
        CashFlowEventConfig(ident4, handler4_1),
        CashFlowEventConfig(ident4, handler4_2),
        CashFlowEventConfig(ident5, handler5),
    )

    should("process") {
        val currYear = yearlyDetailFixture(year, assets = recs)
        val personConfig = personConfigFixture(
            person = personFixture(), cashFlowEvents = eventConfigs)
        val config = configFixture(
            householdConfig = householdConfigFixture(householdMembers = listOf(personConfig))
        )

        val result = CashFlowEventProcessor.process(config, null, currYear)
        result.shouldContainAll(change1, change4_1, change4_1)
        // change 2 is null so change returned,
        // change 5 belongs to prog 5 but there's no matching asset rec, so no change returned
    }
})
