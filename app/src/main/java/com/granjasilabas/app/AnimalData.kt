package com.granjasilabas.app

data class Animal(
    val nombre: String,
    val emoji: String,
    val silabas: List<String>
)

object AnimalData {
    val lista = listOf(
        Animal("VACA",    "🐄",  listOf("VA", "CA")),
        Animal("GALLO",   "🐓",  listOf("GA", "LLO")),
        Animal("PATO",    "🦆",  listOf("PA", "TO")),
        Animal("CABRA",   "🐐",  listOf("CA", "BRA")),
        Animal("CERDO",   "🐷",  listOf("CER", "DO")),
        Animal("OVEJA",   "🐑",  listOf("O", "VE", "JA")),
        Animal("CABALLO", "🐴",  listOf("CA", "BA", "LLO")),
        Animal("GALLINA", "🐔",  listOf("GA", "LLI", "NA")),
        Animal("CONEJO",  "🐰",  listOf("CO", "NE", "JO")),
        Animal("PALOMA",  "🕊️", listOf("PA", "LO", "MA")),
        Animal("BURRO",   "🫏",  listOf("BU", "RRO")),
        Animal("PAVO",    "🦃",  listOf("PA", "VO")),
        Animal("PERRO",   "🐕",  listOf("PE", "RRO")),
        Animal("GATO",    "🐱",  listOf("GA", "TO")),
        Animal("PÁJARO",  "🐦",  listOf("PÁ", "JA", "RO"))
    )
}
