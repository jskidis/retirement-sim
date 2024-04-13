package socsec

import YearMonth
import config.personFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import util.currentDate

class AlreadyClaimingSSBenefitProgressionTest : ShouldSpec({

    should("determine baseAmount and adjustment from claim date, birth date and current amount") {
        val birthYM = YearMonth(currentDate.year - 70)
        val claimYM = YearMonth(currentDate.year - 7)
        val person = personFixture(birthYM = birthYM)
        val currentAmount = 25000.0

        val expectedAdjustment = StdBenefitAdjustmentCalc.calcBenefitAdjustment(birthYM, claimYM)
        val expectedBase = currentAmount / expectedAdjustment

        val result = AlreadyClaimingSSBenefitProgression(person, claimYM, currentAmount)
        result.claimDate(null, null).shouldBe(claimYM)
        result.baseAmount(null, null).shouldBe(expectedBase)
        result.initialAdjustment().shouldBe(expectedAdjustment)
    }
})
