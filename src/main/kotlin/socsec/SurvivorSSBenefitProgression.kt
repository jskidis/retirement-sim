package socsec

import YearMonth
import YearlyDetail
import config.Person
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import tax.TaxabilityProfile
import util.RecFinder

open class SurvivorSSBenefitProgression(
    person: Person,
    val provider: Person,
    taxabilityProfile: TaxabilityProfile,
    benefitAdjCalc: BenefitAdjustmentCalc = SpousalSurvivorBenefitAdjustmentCalc,
    payoutAdjProvider: PayoutAdjProvider = StdPayoutAdjProvider(),
    cmpdInflationProvider: CmpdInflationProvider = StdCmpdInflationProvider(),
) : SecondarySSBenefitProgressionBase(
    person, provider, taxabilityProfile,
    benefitAdjCalc, payoutAdjProvider, cmpdInflationProvider) {

    companion object { const val IDENT_NAME = "SSSurvivor" }
    override fun identName(): String = IDENT_NAME

    override fun providerRec(prevYear: YearlyDetail?, currYear: YearlyDetail): SSBenefitRec? =
        if (prevYear == null) null else RecFinder.findBenefitRec(providerIdent, prevYear)

    override fun programQualification(prevYear: YearlyDetail?): Boolean =
        (prevYear?.departed?.find { it.person == provider.name } != null)

    override fun targetDateQualification(
        currPrimaryRec: SSBenefitRec?, providerRec: SSBenefitRec?): Boolean = true

    override fun newClaimDate(currYear: YearlyDetail,
        currPrimaryRec: SSBenefitRec?, providerRec: SSBenefitRec?): YearMonth =
        YearMonth(year = currYear.year, month = 6)
}

