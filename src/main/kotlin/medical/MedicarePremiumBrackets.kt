package medical

import Amount
import org.apache.commons.csv.CSVRecord
import util.CSVReader

object MedicarePremiumBrackets {
    const val resourcePath = "tables/medicare-prem-agi-based.csv"

    val brackets: List<MedicarePremBracketRec> by lazy { loadBrackets() }

    fun loadBrackets(): List<MedicarePremBracketRec> {
        return getReader().readCsvFromResource(resourcePath).sortedBy { it.singleBracket.start }
    }

    private fun getReader(): CSVReader<MedicarePremBracketRec> = CSVReader { it: CSVRecord ->
        MedicarePremBracketRec(
            singleBracket = MedicareBracket(
                start = it[0].toDouble(),
                end = if (!it[1].isNullOrEmpty()) it[1].toDouble() else Double.MAX_VALUE,
            ),
            jointBracket = MedicareBracket(
                start = it[2].toDouble(),
                end = if (!it[3].isNullOrEmpty()) it[3].toDouble() else Double.MAX_VALUE,
            ),
            partPrems = MedicarePartPrems(
                partBPrem = it[4].toDouble(),
                partDPrem = it[5].toDouble()
            )
        )
    }
}

data class MedicareBracket(
    val start: Amount,
    val end: Amount,
)

data class MedicarePartPrems(
    val partBPrem: Amount,
    val partDPrem: Amount,
)

data class MedicarePremBracketRec(
    val singleBracket: MedicareBracket,
    val jointBracket: MedicareBracket,
    val partPrems: MedicarePartPrems,
)
