package transfers

import YearMonth
import inflation.INFL_TYPE
import inflation.InflationRAC
import inflationRecFixture
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeZero
import io.kotest.matchers.shouldBe
import tax.FilingStatus
import tax.TaxableAmounts
import tax.baseTaxConfigFixture
import util.RetirementLimits
import util.currentDate
import yearlyDetailFixture

class RothIncomeLimitRothConvTest : ShouldSpec({
    val cmpdInflation = 2.0
    val inflationRAC = InflationRAC(rate = .01, cmpdStart = cmpdInflation)
    val inflationRec = inflationRecFixture(stdRAC = inflationRAC)

    val year = currentDate.year + 1
    val currYear = yearlyDetailFixture(year, inflationRec, filingStatus = FilingStatus.JOINTLY)

    val fedTaxable = 10000.0
    val fedLTGTaxable = 1000.0
    val taxableAmounts = TaxableAmounts("Person", fedTaxable, fedLTGTaxable)
    val taxCalcConfig = baseTaxConfigFixture()

    val rothContribLimitJoint = RetirementLimits.calcIRACap(currYear) +
        RetirementLimits.calcIRACatchup(currYear, birthYM = YearMonth(1900))

    val rothIncomeLimitJoint = RetirementLimits.rothIncomeLimit(currYear)

    should(
        "amountToConvert should return difference between roth contribution income cap and taxable amount plus roth contribution max " +
            "if that is less the primary conversion amount provider returned") {

        val primaryAmount = 1000000.0
        val primaryAmountCalc = RothConversionAmountCalc { _, _, _ -> primaryAmount }

        val expectedAmount =
            rothIncomeLimitJoint - rothContribLimitJoint - fedTaxable - fedLTGTaxable

        val amountCalc = RothIncomeLimitRothConv(primaryAmountCalc, INFL_TYPE.STD)
        amountCalc.amountToConvert(currYear, taxableAmounts, taxCalcConfig)
            .shouldBe(expectedAmount)
    }

    should("amountToConvert returns primary conversion amount if its less than income cap minus taxable") {

        val primaryAmount = 100.0
        val primaryAmountCalc = RothConversionAmountCalc { _, _, _ -> primaryAmount }

        val amountCalc = RothIncomeLimitRothConv(primaryAmountCalc, INFL_TYPE.STD)
        amountCalc.amountToConvert(currYear, taxableAmounts, taxCalcConfig).shouldBe(primaryAmount)
    }

    should("amountToConvert wont return less than zero even if both primary and income limit based calc return less than 0") {

        val primaryAmount = -100.0
        val primaryAmountCalc = RothConversionAmountCalc { _, _, _ -> primaryAmount }

        val highTaxableAmounts = TaxableAmounts("Person", 1000000.0)

        val amountCalc = RothIncomeLimitRothConv(primaryAmountCalc, INFL_TYPE.STD)
        amountCalc.amountToConvert(currYear, highTaxableAmounts, taxCalcConfig).shouldBeZero()
    }

    should("amountToConvert factors in filing status") {

        val currYearSingle = currYear.copy(filingStatus = FilingStatus.SINGLE)
        val rothIncomeLimitSingle = RetirementLimits.rothIncomeLimit(currYearSingle)
        val rothContribLimitSingle = RetirementLimits.calcIRACap(currYearSingle) +
            RetirementLimits.calcIRACatchup(currYear, birthYM = YearMonth(1900))


        val primaryAmount = 1000000.0
        val primaryAmountCalc = RothConversionAmountCalc { _, _, _ -> primaryAmount }

        val expectedAmount =
            rothIncomeLimitSingle - rothContribLimitSingle - fedTaxable - fedLTGTaxable

        val amountCalc = RothIncomeLimitRothConv(primaryAmountCalc, INFL_TYPE.STD)
        amountCalc.amountToConvert(currYearSingle, taxableAmounts, taxCalcConfig)
            .shouldBe(expectedAmount)
    }

})
