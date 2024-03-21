import asset.AssetRec
import expense.ExpenseRec
import income.IncomeRec
import inflation.InflationRec
import tax.FilingStatus
import tax.TaxesRec

fun yearlyDetailFixture(
    year: Year = 2024,
    inflation: InflationRec = inflationRateFixture(0.0),
    incomes: List<IncomeRec> = ArrayList(),
    expenses: List<ExpenseRec> = ArrayList(),
    assets: List<AssetRec> = ArrayList(),
    taxes: TaxesRec = TaxesRec(),
    rorRndGaussian: Double = 0.0,
    filingStatus: FilingStatus = FilingStatus.JOINTLY,
) =
    YearlyDetail(
        year = year,
        inflation = inflation,
        incomes = incomes,
        expenses = expenses,
        assets = assets,
        taxes = taxes,
        rorRndGaussian = rorRndGaussian,
        filingStatus = filingStatus
)
