package socsec

import Amount
import Rate
import YearMonth
import YearlyDetail
import config.Person
import tax.TaxabilityProfile

open class FixedDateAmountSSBenefitProgression(
    person: Person,
    targetYM: YearMonth,
    baseAmount: Amount,
    taxabilityProfile: TaxabilityProfile,
) : PrimarySSBenefitProgression(person, taxabilityProfile),
    BenefitBaseAmountProvider by StdBenefitBaseAmountProvider(baseAmount),
    BenefitsClaimDateProvider by StdBenefitsClaimDateProvider(targetYM)

open class IncByIncomeFlexClaimSSBenefitProgression(
    person: Person,
    targetYM: YearMonth,
    baseAmount: Amount,
    incPer100k: Amount,
    multipleOfExpense: Double,
    taxabilityProfile: TaxabilityProfile,
) : PrimarySSBenefitProgression(person, taxabilityProfile),
    BenefitBaseAmountProvider by NewIncomeAdjustBaseAmountProvider(baseAmount, incPer100k),
    BenefitsClaimDateProvider by FlexibleClaimDateProvider(person.birthYM, targetYM, multipleOfExpense)

open class AlreadyClaimingSSBenefitProgression(
    person: Person,
    val claimDate: YearMonth,
    currentAmount: Amount,
    taxabilityProfile: TaxabilityProfile,
)  : PrimarySSBenefitProgression(person, taxabilityProfile) {

    val adjustment by lazy {calcBenefitAdjustment(person.birthYM, claimDate)}
    val baseAmount = currentAmount / adjustment

    override fun baseAmount(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): Amount = baseAmount
    override fun claimDate(prevRec: SSBenefitRec?, prevYear: YearlyDetail?): YearMonth = claimDate
    override fun initialAdjustment(): Rate = adjustment
}
