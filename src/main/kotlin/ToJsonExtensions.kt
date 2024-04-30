import asset.AssetChange
import asset.AssetRec
import departed.DepartedRec
import expense.ExpenseRec
import income.IncomeWithBonusRec
import income.StdIncomeRec
import inflation.InflationRAC
import inflation.InflationRec
import socsec.SSBenefitRec
import tax.TaxableAmounts
import tax.TaxesRec
import transfers.TransferRec
import util.moneyFormat
import util.threeDecimalFormat
import util.twoDecimalFormat

fun RecIdentifier.toJsonStr(): String = "{\"person\":\"$person\", \"name\":\"$name\"}"

fun strWhenNotZero(isZero: Boolean, str: String): String = if (isZero) "" else str

fun YearMonth.toJsonStr(): String = "{" +
    "\"year\":$year" +
    ", \"month\":$month" +
    "}"

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
                proratedGains() == 0.0,
                ", \"capturedGains\":\"${moneyFormat.format(proratedGains())}\"")
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
    strWhenNotZero(taxable == null, ", \"taxable\":$taxable") +
    strWhenNotZero(cashflow == 0.0, ", \"cashFlow\":\"${moneyFormat.format(cashflow)}\"") +
    strWhenNotZero(unrealized == 0.0, ", \"unrealized\":\"${moneyFormat.format(unrealized)}\"") +
    strWhenNotZero(accruedAmt == 0.0, ", \"accrued\":\"${moneyFormat.format(accruedAmt)}\"") +
    "}"

fun ExpenseRec.toJsonStr() = "{" +
    "\"ident\":$ident, " +
    "\"amount\":\"${moneyFormat.format(amount)}\"" +
    strWhenNotZero(taxable().total() == 0.0, ", \"deductions\":${taxable()}") +
    "}"

fun StdIncomeRec.toJsonStr() = "{" +
    "\"ident\":$ident, " +
    "\"amount\":\"${moneyFormat.format(amount)}\"" +
    strWhenNotZero(taxable().total() == 0.0, ", \"taxable\":${taxable()}") +
    "}"

fun IncomeWithBonusRec.toJsonStr() = "{" +
    "\"ident\":$ident, " +
    "\"amount\":\"${moneyFormat.format(baseAmount)}\"" +
    strWhenNotZero(bonus == 0.0, ", \"bonus\":\"${moneyFormat.format(bonus)}\"") +
    strWhenNotZero(taxable().total() == 0.0, ", \"taxable\":${taxable()}") +
    "}"

fun SSBenefitRec.toJsonStr() = "{" +
    "\"ident\":$ident, " +
    "\"amount\":\"${moneyFormat.format(amount)}\"" +
    strWhenNotZero(taxable().total() == 0.0, ", \"taxable\":${taxable()}") +
    strWhenNotZero(baseAmount == 0.0, ", \"base\":\"${moneyFormat.format(baseAmount)}\"") +
    strWhenNotZero(benefitAdjustment == 0.0, ", \"adj\":${twoDecimalFormat.format(benefitAdjustment)}") +
    strWhenNotZero(claimDate == null, ", \"claimDate\":${claimDate}") +
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

fun TransferRec.toJsonStr() = "{" +
    "\"sourceAcct\":${sourceRec.ident}" +
    ", \"distribution\":${sourceTribution}" +
    ", \"destAcct\":${destRec.ident}" +
    ", \"contribution\":${destTribution}" +
    "}"

fun InflationRec.toJsonStr() = "{" +
    "\"rndAjd\": ${threeDecimalFormat.format(rndAdj)}" +
    ", \"std\": ${std}" +
    ", \"wage\": ${wage}" +
    ", \"med\": ${med}" +
    "}"

fun InflationRAC.toJsonStr() = "{" +
    "\"rate\":${threeDecimalFormat.format(rate)}" +
    ", \"start\":${threeDecimalFormat.format(cmpdStart)}" +
    ",\"end\":${threeDecimalFormat.format(cmpdEnd)}" +
    "}"

fun DepartedRec.toJsonStr() = "{" +
    "\"person\":\"$person\"" +
    ", \"year\":$year" +
    "}"

fun YearlyDetail.toJsonStr() = "{" +
    "\"year\":${year}" +
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
    strWhenNotZero(departed.isEmpty(), ", \"departed\":$departed") +
    ", \"taxesTotal\":\"${moneyFormat.format((taxes.total()))}\"" +
    ", \"carryOver\":\"${moneyFormat.format((finalPassTaxes.total() - taxes.total()))}\"" +
    ", \"taxes\":${taxes}" +
    ", \"finalPass\":${finalPassTaxes}" +
    ", \"inflation\":${inflation}" +
    ", \"incomes\":${incomes}" +
    ", \"benefits\":${benefits}" +
    ", \"expenses\":${expenses}" +
    ", \"cashFlows:\":${cashFlowEvents}" +
    ", \"assets\":${assets}" +
    ", \"transfers:\":${transfers}" +
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
