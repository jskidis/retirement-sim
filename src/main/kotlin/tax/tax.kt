package tax

import Amount
import Name
import Rate
import YearlyDetail
import util.moneyFormat
import util.strWhenNotZero

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

    override fun toString(): String =
        ("(Person:$person") +
            strWhenNotZero(fed == 0.0, ", Fed=${moneyFormat.format(fed)}") +
            strWhenNotZero(fedLTG == 0.0, ", FedLTG=${moneyFormat.format(fedLTG)}") +
            strWhenNotZero(state == 0.0, ", State=${moneyFormat.format(state)}") +
            strWhenNotZero(socSec == 0.0, ", SocSec=${moneyFormat.format(socSec)}") +
            strWhenNotZero(medicare == 0.0, ", Medi=${moneyFormat.format(medicare)}") +
            ")"
}

data class TaxesRec(
    val fed: Amount = 0.0,
    val state: Amount = 0.0,
    val socSec: Amount = 0.0,
    val medicare: Amount = 0.0,
    val agi: Amount = 0.0,
) {
    fun total(): Amount = fed + state + socSec + medicare

    override fun toString(): String =
        "(Total: ${moneyFormat.format(total())}" +
            strWhenNotZero(fed == 0.0, ", Fed:${moneyFormat.format(fed)}") +
            strWhenNotZero(state == 0.0, ", State:${moneyFormat.format(state)}") +
            strWhenNotZero(socSec == 0.0, ", SocSoc:${moneyFormat.format(socSec)}") +
            strWhenNotZero(medicare == 0.0, ", Medi:${moneyFormat.format(medicare)}") +
            strWhenNotZero(agi == 0.0, ", AGI:${moneyFormat.format(agi)}") +
            ")"
}

interface TaxCalculator {
    fun determineTax(taxableAmount: Amount, currYear: YearlyDetail): Amount
    fun marginalRate(taxableAmount: Amount, currYear: YearlyDetail): Rate
}

data class TaxCalcConfig(
    val fed: TaxCalculator,
    val fedLTG: TaxCalculator,
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

