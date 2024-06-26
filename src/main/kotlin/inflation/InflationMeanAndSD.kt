package inflation

import Rate

data class InflationMeanAndSD(
    val stdMean: Rate, val stdSD: Rate,
    val medMean: Rate, val medSD: Rate,
    val wageMean: Rate, val wageSD: Rate,
    val housingMean: Rate, val housingSD: Rate,
)

val inflation50YearAvgs = InflationMeanAndSD(
    stdMean = .0396, stdSD = .0295,
    medMean = .0549, medSD = .0293,
    wageMean = .0446, wageSD = .0227,
    housingMean = .0509, housingSD = .0659,
)

val inflation40YearAvgs = InflationMeanAndSD(
    stdMean = .0284, stdSD = .0149,
    medMean = .0435, medSD = .0195,
    wageMean = .0376, wageSD = .0195,
    housingMean = .0430, housingSD = .0553,
)

val inflation30YearAvgs = InflationMeanAndSD(
    stdMean = .0253, stdSD = .0150,
    medMean = .0341, medSD = .0103,
    wageMean = .0359, wageSD = .0189,
    housingMean = .0399, housingSD = .0584,
)

val inflation20YearAvgs = InflationMeanAndSD(
    stdMean = .0257, stdSD = .0181,
    medMean = .0313, medSD = .0107,
    wageMean = .0341, wageSD = .0199,
    housingMean = .0347, housingSD = .0638,
)
