package socsec

import YearMonth
import YearlyDetail
import config.Person
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import tax.TaxabilityProfile
import util.RecFinder

open class SpousalSSBenefitProgression(
    person: Person,
    val provider: Person,
    taxabilityProfile: TaxabilityProfile,
    benefitAdjCalc: BenefitAdjustmentCalc = SpousalBenefitAdjustmentCalc,
    payoutAdjProvider: PayoutAdjProvider = StdPayoutAdjProvider(),
    cmpdInflationProvider: CmpdInflationProvider = StdCmpdInflationProvider(),
) : SecondarySSBenefitProgressionBase(
    person, provider, taxabilityProfile,
    benefitAdjCalc, payoutAdjProvider, cmpdInflationProvider) {

    companion object { const val IDENT_NAME = "SSSpouse" }

    override fun identName(): String = IDENT_NAME

    override fun providerRec(prevYear: YearlyDetail?, currYear: YearlyDetail): SSBenefitRec? =
        RecFinder.findBenefitRec(providerIdent, currYear)

    override fun programQualification(prevYear: YearlyDetail?): Boolean =
        (prevYear?.departed?.find { it.person == provider.name } == null)

    override fun targetDateQualification(
        currPrimaryRec: SSBenefitRec?, providerRec: SSBenefitRec?): Boolean =
        currPrimaryRec?.claimDate != null && providerRec?.claimDate != null

    override fun newClaimDate(currYear: YearlyDetail,
        currPrimaryRec: SSBenefitRec?, providerRec: SSBenefitRec?)
    : YearMonth =
        maxOf(
            currPrimaryRec?.claimDate ?: YearMonth(currYear.year),
            providerRec?.claimDate ?: YearMonth(currYear.year)
        )
}
