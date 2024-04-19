package transfers

import YearlyDetail
import config.SimConfig

object TransferProcessor {
    fun process(config: SimConfig, currYear: YearlyDetail): List<TransferRec> {
        val transferRecs = config.transferGenerators.flatMap {
            val amount = it.determineTransferInfo(config, currYear)
            if (amount <= 0.0) listOf()
            else it.performTransfers(currYear, amount)
        }

        transferRecs.forEach {
            it.sourceRec.tributions.add(it.sourceTribution)
            it.destRec.tributions.add(it.destTribution)
        }
        return transferRecs
    }
}