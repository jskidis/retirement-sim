package util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe

class SCurveDecreasingAmountTest : ShouldSpec({
    val indexes = 10.0 to 20.0
    val values = 1000.0 to 500.0

    should("calc returns appropriate amounts") {
        // If index is less than start index return max value
        SCurveCalc.calcValue(index = indexes.first - 5.0,
            indexRange = indexes, valueRange = values)
            .shouldBe(values.first)

        // At start index should return max value
        SCurveCalc.calcValue(index = indexes.first,
            indexRange = indexes, valueRange = values)
            .shouldBe(values.first)

        // At end index should return min value
        SCurveCalc.calcValue(index = indexes.second,
            indexRange = indexes, valueRange = values)
            .shouldBe(values.second)

        // After end index should return min value
        SCurveCalc.calcValue(index = indexes.second + 5.0,
            indexRange = indexes, valueRange = values)
            .shouldBe(values.second)

        // After mid-point index should return mid-point of value range
        SCurveCalc.calcValue(index = (indexes.first + indexes.second) / 2,
            indexRange = indexes, valueRange = values)
            .shouldBe((values.first +  values.second) / 2)

        // If index is 20% into range returned value should be greater than 80% of value range
        SCurveCalc.calcValue(
            index = (indexes.second - indexes.first) * .2 + indexes.first,
            indexRange = indexes, valueRange = values)
            .shouldBeGreaterThan((values.second - values.first) * .8 + values.first)

        // If index is 80% into range returned value should be less than 20% of value range
        SCurveCalc.calcValue(
            index = (indexes.second - indexes.first) * .8 + indexes.first,
            indexRange = indexes, valueRange = values)
            .shouldBeLessThan((values.second - values.first) * .2 + values.first)
    }
})
