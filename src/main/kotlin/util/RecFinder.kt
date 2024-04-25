package util

import RecIdentifier
import YearlyDetail
import asset.AssetRec
import expense.ExpenseRec
import income.IncomeRec
import socsec.SSBenefitRec

object RecFinder {
    fun findAssetRec(ident: RecIdentifier, currYear: YearlyDetail): AssetRec? =
        currYear.assets.find { it.ident == ident }

    fun findIncomeRec(ident: RecIdentifier, currYear: YearlyDetail): IncomeRec? =
        currYear.incomes.find { it.ident == ident }

    fun findExpenseRec(ident: RecIdentifier, currYear: YearlyDetail): ExpenseRec? =
        currYear.expenses.find { it.ident == ident }

    fun findBenefitRec(ident: RecIdentifier, currYear: YearlyDetail): SSBenefitRec? =
        currYear.benefits.find { it.ident == ident}
}