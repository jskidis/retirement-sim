package socsec

import Amount
import RecIdentifier
import YearMonth
import tax.TaxabilityProfile

open class FixedDateAmountSSBenefitProgression(
    ident: RecIdentifier,
    birthYM: YearMonth,
    targetYM: YearMonth,
    baseAmount: Amount,
    taxabilityProfile: TaxabilityProfile,
) : PrimarySSBenefitProgression(ident, birthYM, taxabilityProfile),
    BenefitBaseAmountProvider by StdBenefitBaseAmountProvider(baseAmount),
    BenefitsTargetDateProvider by StdBenefitsTargetDateProvider(targetYM)
