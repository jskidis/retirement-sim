package tax

import Amount
import Name
import Rate
import YearlyDetail
import toJsonStr

enum class FilingStatus {
    JOINTLY, HOUSEHOLD, SINGLE
}

data class TaxableAmounts(
    val person: Name,
    val fed: Amount = 0.0,
    val fedLTG: Amount = 0.0,
    val state: Amount = 0.0,
    val socSec: Amount = 0.0,
    val medicare: Amount = 0.0,
) {
    fun plus(addend: TaxableAmounts): TaxableAmounts = TaxableAmounts(
        person = this.person,
        fed = this.fed + addend.fed,
        fedLTG = this.fedLTG + addend.fedLTG,
        state = this.state + addend.state,
        socSec = this.socSec + addend.socSec,
        medicare = this.medicare + addend.medicare,
    )

    fun total(): Amount = fed + fedLTG + state + socSec + medicare
    fun hasAmounts(): Boolean = total() != 0.0

    override fun toString(): String = toJsonStr()
}

data class TaxesRec(
    val fed: Amount = 0.0,
    val state: Amount = 0.0,
    val socSec: Amount = 0.0,
    val medicare: Amount = 0.0,
    val agi: Amount = 0.0,
) {
    fun total(): Amount = fed + state + socSec + medicare

    override fun toString(): String = toJsonStr()
}

interface TaxCalculator {
    fun determineTax(taxableAmount: Amount, currYear: YearlyDetail): Amount
}

data class TaxCalcConfig(
    val fed: BracketBasedTaxCalc,
    val fedLTG: BracketBasedTaxCalc,
    val state: TaxCalculator,
    val socSec: TaxCalculator,
    val medicare: TaxCalculator,
)

data class BracketCase(
    val pct: Rate = 0.0,
    val start: Amount = 0.0,
    val end: Amount = Amount.MAX_VALUE,
) {
    fun size() = end - start
}

data class TaxBracket(
    val pct: Rate,
    val jointly: BracketCase = BracketCase(),
    val household: BracketCase = BracketCase(),
    val single: BracketCase = BracketCase(),
)

val currTaxConfig = TaxCalcConfig(
    fed = CurrentFedTaxBrackets,
    fedLTG = CurrentFedLTGBrackets,
    state = CurrentStateTaxBrackets,
    socSec = EmployeeSocSecTaxCalc(),
    medicare = EmployeeMedicareTaxCalc(),
)

val rollbackTaxConfig = TaxCalcConfig(
    fed = RollbackFedTaxBrackets,
    fedLTG = RollbackFedLTGBrackets,
    state = FutureStateTaxBrackets,
    socSec = EmployeeSocSecTaxCalc(),
    medicare = EmployeeMedicareTaxCalc(),
)
