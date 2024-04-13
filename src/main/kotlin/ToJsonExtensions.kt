import asset.AssetChange
import asset.AssetRec
import expense.ExpenseRec
import income.IncomeRec
import inflation.InflationRAC
import socsec.SSBenefitRec
import tax.TaxableAmounts
import tax.TaxesRec
import util.moneyFormat
import util.twoDecimalFormat

fun RecIdentifier.toJsonStr(): String = "{\"person\":\"$person\", \"name\":\"$name\"}"

fun strWhenNotZero(isZero: Boolean, str: String): String = if (isZero) "" else str

fun AssetRec.toJsonStr(): String = "{" +
    "\"ident\":$ident" +
    ", \"startBal\":\"${moneyFormat.format(startBal)}\"" +
    strWhenNotZero(
        startUnrealized == 0.0,
        ", \"startUnrealized\":\"${moneyFormat.format(startUnrealized)}\""
    ) +
    strWhenNotZero(
        totalGains() == 0.0,
        ", \"gains\":$gains, \"totalGains\":\"${moneyFormat.format(totalGains())}\"" +
            strWhenNotZero(
                capturedGains() == 0.0,
                ", \"capturedGains\":\"${moneyFormat.format(capturedGains())}\"")
    ) +
    strWhenNotZero(
        tributions.isEmpty(),
        ", \"tributions\":$tributions, \"netTributions\":\"${moneyFormat.format(totalTributions())}\""
    ) +
    strWhenNotZero(
        totalUnrealized() == 0.0,
        ", \"finalUnrealized\":\"${moneyFormat.format(totalUnrealized())}\""
    ) +
    ", \"finalBal\":\"${moneyFormat.format(finalBalance())}\"" +
    "}"

fun AssetChange.toJsonStr(): String = "{" +
    "\"name\":\"$name\"" +
    ", \"amount\":\"${moneyFormat.format(amount)}\"" +
    strWhenNotZero(unrealized == 0.0, ", \"unrealized\":\"${moneyFormat.format(unrealized)}\"") +
    strWhenNotZero(taxable == null, ", \"taxable\":$taxable") +
    strWhenNotZero(!isCarryOver, ", \"carryover\":true") +
    "}"

fun ExpenseRec.toJsonStr() = "{" +
    "\"ident\":$ident, " +
    "\"amount\":\"${moneyFormat.format(amount)}\"" +
    strWhenNotZero(taxable().total() == 0.0, ", \"deductions\":${taxable()}") +
    "}"

fun IncomeRec.toJsonStr() = "{" +
    "\"ident\":$ident, " +
    "\"amount\":\"${moneyFormat.format(baseAmount)}\"" +
    strWhenNotZero(bonus == 0.0, ", \"bonus\":\"${moneyFormat.format(bonus)}\"") +
    strWhenNotZero(taxable().total() == 0.0, ", \"taxable\":${taxable()}") +
    "}"

fun SSBenefitRec.toJsonStr() = "{" +
    "\"ident\":$ident, " +
    "\"amount\":\"${moneyFormat.format(amount)}\"" +
    strWhenNotZero(taxable().total() == 0.0, ", \"taxable\":${taxable()}") +
    "}"

fun TaxesRec.toJsonStr() = "{" +
    "\"total\":\"${moneyFormat.format(total())}\"" +
    strWhenNotZero(fed == 0.0, ", \"fed\":\"${moneyFormat.format(fed)}\"") +
    strWhenNotZero(state == 0.0, ", \"state\":\"${moneyFormat.format(state)}\"") +
    strWhenNotZero(socSec == 0.0, ", \"socSoc\":\"${moneyFormat.format(socSec)}\"") +
    strWhenNotZero(medicare == 0.0, ", \"medicare\":\"${moneyFormat.format(medicare)}\"") +
    strWhenNotZero(agi == 0.0, ", \"AGI\":\"${moneyFormat.format(agi)}\"") +
    "}"

fun TaxableAmounts.toJsonStr() = "{" +
    strWhenNotZero(total() == 0.0, "\"person\":\"$person\"") +
    strWhenNotZero(fed == 0.0, ", \"fed\":\"${moneyFormat.format(fed)}\"") +
    strWhenNotZero(fedLTG == 0.0, ", \"fedLTG\":\"${moneyFormat.format(fedLTG)}\"") +
    strWhenNotZero(state == 0.0, ", \"state\":\"${moneyFormat.format(state)}\"") +
    strWhenNotZero(socSec == 0.0, ", \"socSec\":\"${moneyFormat.format(socSec)}\"") +
    strWhenNotZero(medicare == 0.0, ", \"medicare\":\"${moneyFormat.format(medicare)}\"") +
    "}"

fun InflationRAC.toJsonStr() = "{" +
    "\"rate\":${twoDecimalFormat.format(rate)}" +
    ",\"compound\":${twoDecimalFormat.format(cmpdEnd)}" +
    "}"

fun YearlyDetail.toJsonStr() = "{" +
    "\"year\": ${year}" +
    ", \"income\":\"${moneyFormat.format(totalIncome())}\"" +
    strWhenNotZero(
        totalBenefits() == 0.0,
        ", \"benefit\":\"${moneyFormat.format(totalBenefits())}\"") +
    strWhenNotZero(
        totalAssetCashflow() == 0.0,
        ", \"assetCashflow\":\"${moneyFormat.format((totalAssetCashflow()))}\"") +
    ", \"expense\":\"${moneyFormat.format(totalExpense())}\"" +
    ", \"assetValue\":\"${moneyFormat.format(totalAssetValues())}\"" +
    ", \"averageROR\":\"${twoDecimalFormat.format(averageRor() * 100)}\"" +
    ", \"infAdj\":\"${moneyFormat.format(totalAssetValues() / inflation.std.cmpdEnd)}\"" +
    ", \"netSpend\":\"${moneyFormat.format((netSpend()))}\"" +
    ", \"netDist\":\"${moneyFormat.format((netDistributions()))}\"" +
    ", \"taxesTotal\":\"${moneyFormat.format((taxes.total()))}\"" +
    ", \"carryOver\":\"${moneyFormat.format((finalPassTaxes.total() - taxes.total()))}\"" +
    ", \"inflation\": ${inflation.std}" +
    ", \"taxes\":${taxes}" +
    ", \"finalPass\":${finalPassTaxes}" +
    ", \"incomes\":${incomes}" +
    ", \"benefits\":${benefits}" +
    ", \"expenses\":${expenses}" +
    ", \"assets\":${assets}" +
    "}"

fun yearlySummaryHeaders(): String =
    "Sim #, Year, Inflation, Assets, Inf Adj, Avg ROR, Incomes, Benefits, Expenses, Cashflow Events, Net Spend, AGI, Taxes, Payroll Taxes"

fun YearlySummary.toCSV(simNum: Int): String =
    "$simNum, $year" +
        ", ${inflation}" +
        ", ${assetValue}" +
        ", ${inflAdjAssets()}" +
        ", ${avgROR}" +
        ", ${income}" +
        ", ${benefits}" +
        ", ${expenses}" +
        ", ${cashflowEvents}" +
        ", ${netSpend} " +
        ", ${agi} " +
        ", ${taxes} " +
        ", ${payrollTaxes} "
