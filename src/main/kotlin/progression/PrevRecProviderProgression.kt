package progression

import YearlyDetail

interface PrevRecProviderProgression<T> :
    Progression<T> {

    fun initialValue(): T
    fun previousValue(prevYear: YearlyDetail): T
    fun next(prevVal: T): T

    override fun determineNext(prevYear: YearlyDetail?): T =
        if (prevYear == null) initialValue()
        else next(previousValue(prevYear))
}

