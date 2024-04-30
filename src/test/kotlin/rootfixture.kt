import asset.AssetRec
import departed.DepartedRec
import expense.ExpenseRec
import income.IncomeRec
import inflation.InflationRec
import socsec.SSBenefitRec
import tax.FilingStatus
import tax.TaxesRec
import util.currentDate

fun yearlyDetailFixture(
    year: Year = currentDate.year + 1,
    inflation: InflationRec = inflationRateFixture(0.0),
    incomes: List<IncomeRec> = ArrayList(),
    expenses: List<ExpenseRec> = ArrayList(),
    benefits: List<SSBenefitRec> = ArrayList(),
    assets: List<AssetRec> = ArrayList(),
    taxes: TaxesRec = TaxesRec(),
    secondPassTaxes: TaxesRec = TaxesRec(),
    departed: List<DepartedRec> = ArrayList(),
    randomValues: Map<String, Double> = mapOf(),
    filingStatus: FilingStatus = FilingStatus.JOINTLY,
) =
    YearlyDetail(
        year = year,
        inflation = inflation,
        incomes = incomes,
        expenses = expenses,
        benefits = benefits,
        assets = assets,
        taxes = taxes,
        finalPassTaxes = secondPassTaxes,
        departed = departed,
        randomValues = randomValues,
        filingStatus = filingStatus
)
