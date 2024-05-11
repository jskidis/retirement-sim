package departed

import Rate
import config.ActuarialGender
import org.apache.commons.csv.CSVRecord
import util.CSVReader

data class ActuarialLifeRec(
    val age: Int,
    val malePct: Rate,
    val femalePct: Rate
)

object ActuarialLifeMap : ChanceOfDeathCalc {
    val ageMap: Map<Int, ActuarialLifeRec> by lazy {
        loadMap().associate { Pair(it.age, it) }
    }

    fun loadMap(): List<ActuarialLifeRec> =
        getReader().readCsvFromResource("tables/actuarial-life.csv")


    fun getReader() = CSVReader { it: CSVRecord ->
        ActuarialLifeRec(
            age = it[0].toInt(),
            malePct = it[1].toDouble(),
            femalePct = it[2].toDouble()
        )
    }

    override fun getChanceOfDeath(age: Int, gender: ActuarialGender): Double =
        when (gender) {
            ActuarialGender.MALE -> ageMap.get(age)?.malePct ?: 0.0
            ActuarialGender.FEMALE -> ageMap.get(age)?.femalePct ?: 0.0
        }

}

fun interface ChanceOfDeathCalc {
    fun getChanceOfDeath(age: Int, gender: ActuarialGender): Double
}
