package socsec

import RecIdentifier
import YearlyDetail
import config.Person
import inflation.CmpdInflationProvider
import inflation.StdCmpdInflationProvider
import tax.TaxabilityProfile
import util.RecFinder

open class SpousalSSBenefitProgression(
    val person: Person,
    spouse: Person,
    val taxabilityProfile: TaxabilityProfile,
    val benefitAdjCalc: BenefitAdjustmentCalc = SpousalBenefitAdjustmentCalc,
    val payoutAdjProvider: PayoutAdjProvider = StdPayoutAdjProvider(),
    val cmpdInflationProvider: CmpdInflationProvider = StdCmpdInflationProvider(),
) : SecondarySSBenefitProgression {

    companion object {
        const val IDENT_NAME = "SSSpousal"
    }

    val secondaryIdent = RecIdentifier(IDENT_NAME, person.name)
    val primaryIdent = RecIdentifier(PrimarySSBenefitProgression.IDENT_NAME, person.name)
    val spouseIdent = RecIdentifier(PrimarySSBenefitProgression.IDENT_NAME, spouse.name)

    override fun determineNext(prevYear: YearlyDetail?, currYear: YearlyDetail): SSBenefitRec {
        val prevSecondaryRec = prevYear?.let { RecFinder.findBenefitRec(secondaryIdent, prevYear) }
        val currPrimaryRec = RecFinder.findBenefitRec(primaryIdent, currYear)
        val currSpouseRec = RecFinder.findBenefitRec(spouseIdent, currYear)

        val (baseAmount, benefitAdj, claimDate) = when {
            prevSecondaryRec?.claimDate != null -> Triple(
                prevSecondaryRec.baseAmount,
                prevSecondaryRec.benefitAdjustment,
                prevSecondaryRec.claimDate
            )

            currPrimaryRec?.claimDate != null && currSpouseRec?.claimDate != null -> {
                val startYM = maxOf(currPrimaryRec.claimDate, currSpouseRec.claimDate)
                Triple(
                    currSpouseRec.baseAmount,
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
        val amountMinusPrimary = Math.max(0.0,
            (fullAmount - (currPrimaryRec?.amount ?: 0.0)) * pctInYear)
        val amount = payoutAdjProvider.adjustPayout(amountMinusPrimary)

        return SSBenefitRec(
            year = currYear.year,
            ident = secondaryIdent,
            amount = amount,
            taxableAmount = taxabilityProfile.calcTaxable(person.name, amount),
            baseAmount = baseAmount,
            benefitAdjustment = benefitAdj,
            claimDate = claimDate
        )
    }
}
