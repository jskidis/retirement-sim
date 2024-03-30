package medical

import YearMonth
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe
import util.ConstantsProvider
import util.ConstantsProvider.KEYS.MARKETPLACE_BASE_PREM
import yearlyDetailFixture

class MarketplacePremProgressionTest : FunSpec({

    val year = 2021
    val cmpdInflation = 1.2
    val inflation = inflationRecFixture(
        medRAC = InflationRAC(0.05, cmpdInflation, cmpdInflation + 0.05))

    val person21YO = YearMonth(year = year - 21, month = 0)
    val person41YO = YearMonth(year = year - 41, month = 0)

    val currYear = yearlyDetailFixture(year = year, inflation = inflation)

    test("determineNext should return base premium amount (base plus medical inflation) for Silver HMO at age 21 ") {
        val progression = MarketplacePremProgressionFixture(
            birthYM = person21YO, medalType = MPMedalType.SILVER, planType = MPPlanType.HMO)

        val expectedPremium = ConstantsProvider.getValue(MARKETPLACE_BASE_PREM) * cmpdInflation
        val results = progression.determineNext(currYear, previousAGI = 0.0)
        results.premium.shouldBe(expectedPremium)
        results.monthsCovered.shouldBe(12)
        results.fullyDeductAmount.shouldBe(0.0)
        results.name.shouldBe(MarketplacePremProgression.DESCRIPTION)
    }

    test("determineNext should factor in age into premium") {
        val progression = MarketplacePremProgressionFixture(
            birthYM = person41YO, medalType = MPMedalType.SILVER, planType = MPPlanType.HMO)

        // The progression fixture adds 0.01 to factor for year in age over 21, so 41 YO would have a factor increased to 1.2
        val expectedPremium = ConstantsProvider.getValue(MARKETPLACE_BASE_PREM) * cmpdInflation * 1.2

        val results = progression.determineNext(currYear, previousAGI = 0.0 )
        results.premium.shouldBe(expectedPremium)
    }

    test("determineNext should factor in medal type into premium") {
        val progression = MarketplacePremProgressionFixture(
            birthYM = person21YO, medalType = MPMedalType.GOLD, planType = MPPlanType.HMO)

        // The progression fixture has 10% increase to factor for GOLD compare to silver,
        val expectedPremium = ConstantsProvider.getValue(MARKETPLACE_BASE_PREM) * cmpdInflation * 1.1

        val results = progression.determineNext(currYear, previousAGI = 0.0 )
        results.premium.shouldBe(expectedPremium)
    }

    test("determineNext should factor plan type into premium") {
        val progression = MarketplacePremProgressionFixture(
            birthYM = person21YO, medalType = MPMedalType.SILVER, planType = MPPlanType.PPO)

        // The progression fixture has 20% increase to factor for PPO compare to HMO,
        val expectedPremium = ConstantsProvider.getValue(MARKETPLACE_BASE_PREM) * cmpdInflation * 1.2

        val results = progression.determineNext(currYear, previousAGI = 0.0 )
        results.premium.shouldBe(expectedPremium)
    }
})

class MarketplacePremProgressionFixture(
    birthYM: YearMonth, medalType: MPMedalType, planType: MPPlanType,
) : MarketplacePremProgression(birthYM, medalType, planType) {

    override fun getAgeFactor(age: Int): Double = 1.0 + ((age - 21) * .01)

    override fun getMedalPlanFactor(medal: MPMedalType, plan: MPPlanType): Double =
        0.9 + (medal.ordinal * 0.1) + (plan.ordinal * 0.1)
}

class MPAgeMapTest : FunSpec({
    test("loads successful and returns factor for age (factor should increase after 21)") {
        val factor21YO = MPAgeMap.getAgeFactor(21)
        val factor41YO = MPAgeMap.getAgeFactor(41)
        val factor61YO = MPAgeMap.getAgeFactor(61)
        factor21YO.shouldBeLessThan(factor41YO)
        factor41YO.shouldBeLessThan(factor61YO)
    }
})

class MPMedalPlanMapTest : FunSpec({
    test("loads successful and returns factor for medal and plan") {
        val silverHMO =  MPMedalPlanMap.getMedalPlanFactor(MPMedalType.SILVER, MPPlanType.HMO)
        val silverEPO =  MPMedalPlanMap.getMedalPlanFactor(MPMedalType.SILVER, MPPlanType.EPO)
        val silverPPO =  MPMedalPlanMap.getMedalPlanFactor(MPMedalType.SILVER, MPPlanType.PPO)
        silverHMO.shouldBeLessThan(silverEPO)
        silverEPO.shouldBeLessThan(silverPPO)

        val bronzeHMO = MPMedalPlanMap.getMedalPlanFactor(MPMedalType.BRONZE, MPPlanType.HMO)
        val goldHMO = MPMedalPlanMap.getMedalPlanFactor(MPMedalType.GOLD, MPPlanType.HMO)
        val platinumHMO = MPMedalPlanMap.getMedalPlanFactor(MPMedalType.PLATINUM, MPPlanType.HMO)
        bronzeHMO.shouldBeLessThan(silverHMO)
        silverHMO.shouldBeLessThan(goldHMO)
        goldHMO.shouldBeLessThan(platinumHMO)
    }
})

