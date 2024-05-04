package transfers

import Amount
import Name
import Year
import YearlyDetail
import config.SimConfig

interface ByYearAmountDeterminer : TransferAmountDeterminer {
    fun yearToTransfer(): Year
    override fun determineTransferAmount(config: SimConfig, currYear: YearlyDetail): Amount =
        if (currYear.year != yearToTransfer()) 0.0
        else availableToTransfer(currYear)
}

interface OnDepartureAmountDeterminer : TransferAmountDeterminer {
    fun departedPerson(): Name
    override fun determineTransferAmount(config: SimConfig, currYear: YearlyDetail): Amount =
        if (currYear.departed.find { it.person == departedPerson() } == null) 0.0
        else availableToTransfer(currYear)
}