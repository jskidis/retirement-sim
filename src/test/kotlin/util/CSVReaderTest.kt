package util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import org.apache.commons.csv.CSVRecord

class CSVReaderTest : ShouldSpec({
    data class TestEntity(
        val int: Int, val dbl: Double, val str: String, val defaultable: String,
    )

    val convertF = { it: CSVRecord ->
        TestEntity(
            int = it[0].toInt(),
            dbl = it[1].toDouble(),
            str = it[2],
            defaultable = if (it.size() > 3 && !it[3].isNullOrEmpty()) it[3] else "defaulted value"
        )
    }

    val reader = CSVReader(convertF)

    should("readCsvFromResource loads csv from file and populate entities based on passed in conversion function") {
        val result = reader.readCsvFromResource("csvReaderTest.csv")
        result.size.shouldBe(3)
        result[0].shouldBe(TestEntity(1, 10.0, "1st string", "default has value"))
        result[1].shouldBe(TestEntity(2, 20.0, "2nd string", "defaulted value"))
        result[2].shouldBe(TestEntity(3, 30.0, "3rd string", "defaulted value"))
    }
})
