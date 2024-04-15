package cashflow

import RecIdentifier
import asset.*
import config.configFixture
import config.householdConfigFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainAll
import util.currentDate
import yearlyDetailFixture

class CashFlowEventProcessorTest : ShouldSpec({

    val year = currentDate.year +1

    val gains = AssetChange("Gains", 0.0)
    val gainCreator = AssetGainCreator {
        _, person, _, _ -> AssetChange(person, 0.0)
    }

    val ident1 = RecIdentifier(name = "Asset 1", person = "Person")
    val ident2 = RecIdentifier(name = "Asset 2", person = "Person")
    val ident3 = RecIdentifier(name = "Asset 3", person = "Person")
    val ident4 = RecIdentifier(name = "Asset 4", person = "Person")
    val ident5 = RecIdentifier(name = "Asset 5", person = "Person")

    val rec1 = AssetRec(year, ident1, AssetType.IRA,0.0, 0.0, gains)
    val rec2 = AssetRec(year, ident2, AssetType.ROTH,0.0, 0.0, gains)
    val rec3 =  AssetRec(year, ident3, AssetType.STD401K, 0.0, 0.0, gains)
    val rec4 = AssetRec(year, ident4, AssetType.ROTH401K, 0.0, 0.0,  gains)
    val recs = listOf(rec1, rec2, rec3, rec4)

    val change1 = AssetChange("CFE1", 2000.0, null)
    val change2: AssetChange? = null
    val change4_1 = AssetChange("CFE4-1", -3000.0, null)
    val change4_2 = AssetChange("CFE4-2", 4000.0, null)
    val change5 = AssetChange("CFE5", 1000.0, null)

    val handler1 = CashFlowEventHandler{_, _ -> change1}
    val handler2 = CashFlowEventHandler{_, _ -> change2}
    val handler4_1 = CashFlowEventHandler{_, _ -> change4_1}
    val handler4_2 = CashFlowEventHandler{_, _ -> change4_2}
    val handler5 = CashFlowEventHandler{_, _ -> change5}

    val startBal = 0.0
    val prog1 = AssetProgression(ident1, AssetType.IRA, startBal, gainCreator, listOf(handler1))
    val prog2 = AssetProgression(ident2, AssetType.ROTH, startBal, gainCreator, listOf(handler2))
    val prog3 = AssetProgression(ident3, AssetType.STD401K, startBal, gainCreator, listOf())
    val prog4 = AssetProgression(ident4, AssetType.ROTH401K, startBal, gainCreator, listOf(handler4_1, handler4_2))
    val prog5 = AssetProgression(ident5, AssetType.NRA, startBal, gainCreator, listOf(handler5))
    val progs = listOf(prog1, prog2, prog3, prog4, prog5)

    should("process") {
        val currYear = yearlyDetailFixture(year, assets = recs)
        val config = configFixture(householdConfig = householdConfigFixture(jointAssets = progs))

        val result = CashFlowEventProcessor.process(config, currYear)
        result.shouldContainAll(change1, change4_1, change4_1)
        // change 2 is null so change returned,
        // change 5 belongs to prog 5 but there's no matching asset rec, so no change returned
    }
})
