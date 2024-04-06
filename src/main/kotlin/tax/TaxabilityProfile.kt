package tax

import Amount
import Name

interface TaxabilityProfile {
    fun fed(amount: Amount): Amount
    fun state(amount: Amount): Amount
    fun socSec(amount: Amount): Amount
    fun medicare(amount: Amount): Amount

    fun calcTaxable(person: Name, amount: Amount) = TaxableAmounts(
        person = person,
        fed = fed(amount),
        state = state(amount),
        socSec = socSec(amount),
        medicare = medicare(amount),
    )
}

open class WageTaxableProfile : FedTaxableProfile, StateTaxableProfile, PayrollTaxableProfile
open class NonWageTaxableProfile : FedTaxableProfile, StateTaxableProfile, NonPayrollTaxableProfile
open class FedOnlyTaxableProfile : FedTaxableProfile, NotStateTaxableProfile, NonPayrollTaxableProfile
open class FedAndStateDeductProfile : FedDeductProfile, StateDeductProfile, NonPayrollTaxableProfile
open class FullyDeductProfile : FedDeductProfile, StateDeductProfile, PayrollTaxDeductProfile

open class UnusedProfile : NotFedTaxableProfile, NotStateTaxableProfile, NonPayrollTaxableProfile
typealias NonTaxableProfile = UnusedProfile
typealias NonDeductProfile = NonTaxableProfile

class SSBenefitTaxableProfile : NonPayrollTaxableProfile, NotStateTaxableProfile {
    override fun fed(amount: Amount): Amount = amount * 0.85
}

interface FedTaxableProfile : TaxabilityProfile {
    override fun fed(amount: Amount): Amount = amount
}

interface NotFedTaxableProfile : TaxabilityProfile {
    override fun fed(amount: Amount): Amount = 0.0
}

interface StateTaxableProfile : TaxabilityProfile {
    override fun state(amount: Amount): Amount = amount
}

interface NotStateTaxableProfile : TaxabilityProfile {
    override fun state(amount: Amount): Amount = 0.0
}

interface PayrollTaxableProfile : TaxabilityProfile {
    override fun socSec(amount: Amount): Amount = amount
    override fun medicare(amount: Amount): Amount = amount
}

interface NonPayrollTaxableProfile : TaxabilityProfile {
    override fun socSec(amount: Amount): Amount = 0.0
    override fun medicare(amount: Amount): Amount = 0.0
}

interface FedDeductProfile : TaxabilityProfile {
    override fun fed(amount: Amount): Amount = -amount
}

interface StateDeductProfile : TaxabilityProfile {
    override fun state(amount: Amount): Amount = -amount
}

interface PayrollTaxDeductProfile : TaxabilityProfile {
    override fun socSec(amount: Amount): Amount = -amount
    override fun medicare(amount: Amount): Amount = -amount
}
