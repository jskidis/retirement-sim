package config

import Name
import YearMonth
import inflationConfigFixture
import tax.taxConfigFixture

fun configFixture() =
    MainConfig(
        startYear = 2020,
        householdMembers = householdMembersFixture(),
        inflationConfig = inflationConfigFixture(),
        taxConfig = taxConfigFixture()
    )

fun householdMembersFixture(parent1: Name = "Parent 1", parent2: Name = "Parent 2",
    dependants: List<Name> = listOf()) =
    HouseholdMembers(
        parent1 = parentFixture(parent1),
        parent2 = parentFixture(parent2),
        dependants = dependants.map { dependantFixture(it) }
    )


fun parentFixture(name: Name, birthYM: YearMonth = YearMonth(year = 1980, month = 0)) =
    Parent(name, birthYM)

fun dependantFixture(name: Name, birthYM: YearMonth = YearMonth(2000, 0)) =
    Dependant(name, birthYM)


