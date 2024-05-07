package socsec

import RecIdentifier
import YearMonth
import YearlyDetail
import config.Person
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import tax.TaxabilityProfile
import util.RecFinder

abstract class SecondarySSBenefitProgressionBase(
    val person: Person,
    provider: Person,
    val taxabilityProfile: TaxabilityProfile,
    val benefitAdjCalc: BenefitAdjustmentCalc = SpousalBenefitAdjustmentCalc,
    val payoutAdjProvider: PayoutAdjProvider = StdPayoutAdjProvider(),
    val cmpdInflationProvider: CmpdInflationProvider = StdCmpdInflationProvider(),
) : SecondarySSBenefitProgression {

    abstract fun identName(): String
    abstract fun providerRec(prevYear: YearlyDetail?, currYear: YearlyDetail): SSBenefitRec?
    abstract fun programQualification(prevYear: YearlyDetail?): Boolean

    abstract fun targetDateQualification(
        currPrimaryRec: SSBenefitRec?, providerRec: SSBenefitRec?): Boolean

    abstract fun newClaimDate(currYear: YearlyDetail,
        currPrimaryRec: SSBenefitRec?, providerRec: SSBenefitRec?): YearMonth

    val secondaryIdent = RecIdentifier(identName(), person.name)
    val primaryIdent = RecIdentifier(PrimarySSBenefitProgression.IDENT_NAME, person.name)
    val providerIdent = RecIdentifier(PrimarySSBenefitProgression.IDENT_NAME, provider.name)

    override fun determineNext(prevYear: YearlyDetail?, currYear: YearlyDetail): SSBenefitRec {
        val prevSecondaryRec = prevYear?.let { RecFinder.findBenefitRec(secondaryIdent, prevYear) }
        val currPrimaryRec = RecFinder.findBenefitRec(primaryIdent, currYear)
        val providerRec = providerRec(prevYear, currYear)

        val (baseAmount, benefitAdj, claimDate) = when {
            !programQualification(prevYear) -> Triple(0.0, 0.0, null)

            prevSecondaryRec?.claimDate != null -> {
                val benefitAdj = prevSecondaryRec.benefitAdjustment
                val newBenefitAdj =
                    if(benefitAdj > 0.0) benefitAdj
                    else benefitAdjCalc.calcBenefitAdjustment(
                        person.birthYM, YearMonth(currYear.year))

                if (newBenefitAdj == benefitAdj) Triple(
                    prevSecondaryRec.baseAmount,
                    prevSecondaryRec.benefitAdjustment,
                    prevSecondaryRec.claimDate
                )
                else Triple(
                    prevSecondaryRec.baseAmount,
                    newBenefitAdj,
                    YearMonth(currYear.year)
                )
            }

            providerRec != null && targetDateQualification(currPrimaryRec, providerRec) -> {
                val startYM = newClaimDate(currYear, currPrimaryRec, providerRec)
                Triple(
                    providerRec.baseAmount,
                    benefitAdjCalc.calcBenefitAdjustment(person.birthYM, startYM),
                    startYM
                )
            }

            else -> Triple(0.0, 0.0, null)
        }

        val pctInYear =
            if (claimDate?.year != currYear.year) 1.0
            else 1 - claimDate.monthFraction()

        val cmpInflation = cmpdInflationProvider.getCmpdInflationStart(currYear)
        val fullAmount = baseAmount * benefitAdj * cmpInflation
        val amountMinusPrimary = Math.max(
            0.0,
            (fullAmount - (currPrimaryRec?.amount ?: 0.0)) * pctInYear)
        val amount = payoutAdjProvider.adjustPayout(amountMinusPrimary, currYear.year)

        return SSBenefitRec(
            year = currYear.year,
            ident = secondaryIdent,
            amount = amount,
            taxableAmount = taxabilityProfile.calcTaxable(person.name, amount),
            baseAmount = baseAmount,
            benefitAdjustment = benefitAdj,
            claimDate = claimDate,
            alwaysRetain = false
        )
    }
}
