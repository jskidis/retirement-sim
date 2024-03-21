package tax

import Amount
import Name
import Rate
import YearlyDetail
import util.moneyFormat

enum class FilingStatus{
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

    override fun toString(): String {
        val fedStr = if (fed != 0.0) "Fed=${moneyFormat.format(fed)}, " else ""
        val fedLTGStr = if (fedLTG != 0.0) "FedLTG=${moneyFormat.format(fedLTG)}, " else ""
        val stateStr = if (state != 0.0) "State=${moneyFormat.format(state)}, " else ""
        val socSecStr = if (socSec != 0.0) "SocSec=${moneyFormat.format(socSec)}, " else ""
        val medicareStr = if (medicare != 0.0) "Medi=${moneyFormat.format(medicare)}, " else ""
        val fullStr = "$fedStr$fedLTGStr$stateStr$socSecStr$medicareStr"
        val trimmedStr = if(fullStr.length < 2) fullStr else fullStr.dropLast(2)
        return "(Person:$person, $trimmedStr)"
    }
}

data class TaxesRec(
    val fed: Amount = 0.0,
    val state: Amount = 0.0,
    val socSec: Amount = 0.0,
    val medicare: Amount = 0.0,
) {
    fun total(): Amount = fed + state + socSec + medicare

    override fun toString(): String {
        val fedStr = if (fed != 0.0) "Fed:${moneyFormat.format(fed)} " else ""
        val stateStr = if (state != 0.0) "State:${moneyFormat.format(state)} " else ""
        val socSecStr = if (socSec != 0.0) "SocSec:${moneyFormat.format(socSec)} " else ""
        val medicareStr = if (medicare != 0.0) "Medi:${moneyFormat.format(medicare)} " else ""
        return "(${moneyFormat.format(total())}-$fedStr$stateStr$socSecStr$medicareStr)"
    }
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
    val medicare: TaxCalculator
)

data class BracketCase(
    val pct: Rate = 0.0,
    val start: Amount = 0.0,
    val end: Amount= Amount.MAX_VALUE,
) {
    fun size() = end - start
}

data class TaxBracket(
    val pct: Rate,
    val jointly: BracketCase = BracketCase(),
    val household: BracketCase = BracketCase(),
    val single: BracketCase = BracketCase()
)

