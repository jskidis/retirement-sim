import asset.AssetChange
import asset.AssetRec
import expense.ExpenseRec
import income.IncomeRec
import socsec.SSBenefitRec
import tax.TaxableAmounts
import tax.TaxesRec
import util.moneyFormat
import util.strWhenNotZero

fun RecIdentifier.toJsonStr(): String = "{\"person\":\"$person\", \"name\":\"$name\"}"

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
    "}"

fun ExpenseRec.toJsonStr() = "{" +
    "\"ident\":$ident, " +
    "\"amount\":\"${moneyFormat.format(amount)}\"" +
    strWhenNotZero(taxDeductions.total() == 0.0, ", \"deductions\":$taxDeductions") +
    "}"

fun IncomeRec.toJsonStr() = "{" +
    "\"ident\":$ident, " +
    "\"amount\":\"${moneyFormat.format(baseAmount)}\"" +
    strWhenNotZero(bonus == 0.0, ", \"bonus\":\"${moneyFormat.format(bonus)}\"") +
    strWhenNotZero(taxableIncome.total() == 0.0, ", \"taxable\":${taxableIncome}") +
    "}"

fun SSBenefitRec.toJsonStr() = "{" +
    "\"ident\":$ident, " +
    "\"amount\":\"${moneyFormat.format(amount)}\"" +
    strWhenNotZero(taxableAmount.total() == 0.0, ", \"taxable\":${taxableAmount}") +
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

fun YearlyDetail.toJsonStr() = "{" +
    "\"year\": ${year}" +
    ", \"income\":\"${moneyFormat.format(totalIncome())}\"" +
    ", \"benefit\":\"${moneyFormat.format(totalBenefits())}\"" +
    ", \"expense\":\"${moneyFormat.format(totalExpense())}\"" +
    ", \"assetValue\":\"${moneyFormat.format(totalAssetValues())}\"" +
    ", \"infAdj\":\"${moneyFormat.format(totalAssetValues() / inflation.std.cmpdEnd)}\"" +
    ", \"netSpend\":\"${moneyFormat.format((netSpend()))}\"" +
    ", \"taxesTotal\":\"${moneyFormat.format((taxes.total()))}\"" +
    ", \"carryOver\":\"${moneyFormat.format((secondPassTaxes.total() - taxes.total()))}\"" +
    ", \"secondPass\":${secondPassTaxes}" +
    ", \"taxes\":${taxes}" +
    ", \"secondPass\":${secondPassTaxes}" +
    ", \"incomes\":${incomes}" +
    ", \"benefits\":${benefits}" +
    ", \"expenses\":${expenses}" +
    ", \"assets\":${assets}" +
    "}"