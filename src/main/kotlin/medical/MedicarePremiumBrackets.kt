package medical

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
                start = if(!it[0].isNullOrEmpty()) it[0].toDouble() else -Double.MAX_VALUE,
                end = if (!it[1].isNullOrEmpty()) it[1].toDouble() else Double.MAX_VALUE,
            ),
            jointBracket = MedicareBracket(
                start = if(!it[2].isNullOrEmpty()) it[2].toDouble() else -Double.MAX_VALUE,
                end = if (!it[3].isNullOrEmpty()) it[3].toDouble() else Double.MAX_VALUE,
            ),
            partPrems = MedicarePartPrems(
                partBPrem = it[4].toDouble(),
                partDPrem = it[5].toDouble(),
                medigap = it[6].toDouble(),
                dental = it[7].toDouble(),
            )
        )
    }
}
