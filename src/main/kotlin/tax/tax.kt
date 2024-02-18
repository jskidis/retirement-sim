package tax

import Amount
import Name
import YearlyDetail
import config.MainConfig
import moneyFormat

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

    fun hasAmounts(): Boolean =
        fed != 0.0 || fedLTG != 0.0 || state != 0.0 || socSec != 0.0 || medicare != 0.0

    override fun toString(): String {
        val fedStr = if (fed != 0.0) "Fed:${moneyFormat.format(fed)} " else ""
        val fedLTGStr = if (fedLTG != 0.0) "Fed:${moneyFormat.format(fedLTG)} " else ""
        val stateStr = if (state != 0.0) "State:${moneyFormat.format(state)} " else ""
        val socSecStr = if (socSec != 0.0) "SocSec:${moneyFormat.format(socSec)} " else ""
        val medicareStr = if (fed != 0.0) "Medi:${moneyFormat.format(medicare)} " else ""
        return "$person-($fedStr$fedLTGStr$stateStr$socSecStr$medicareStr)"
    }
}

data class TaxesRec(
    val fed: Amount,
    val state: Amount,
    val socSec: Amount,
    val medicare: Amount,
) {
    fun total(): Amount = fed + state + socSec + medicare

    override fun toString(): String {
        val fedStr = if (fed != 0.0) "Fed:${moneyFormat.format(fed)} " else ""
        val stateStr = if (state != 0.0) "State:${moneyFormat.format(state)} " else ""
        val socSecStr = if (socSec != 0.0) "SocSec:${moneyFormat.format(socSec)} " else ""
        val medicareStr = if (medicare != 0.0) "Medi:${moneyFormat.format(medicare)} " else ""
        return "(Total: ${moneyFormat.format(total())}-$fedStr$stateStr$socSecStr$medicareStr)"
    }
}

interface TaxCalculator {
    fun determineTax(taxableAmount: Amount, config: MainConfig, currYear: YearlyDetail): Amount
}