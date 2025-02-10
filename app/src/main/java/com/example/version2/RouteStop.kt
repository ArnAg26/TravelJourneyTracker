data class RouteStop(
    val name: String,
    val distance: Int,
    val visaRequirement: String,
    var distanceCovered: Int
)

fun sampleStops() = listOf(
    RouteStop("Seoul", 500, "No Visa", 100),
    RouteStop("Tokyo", 700, "Visa Required", 300),
    RouteStop("Sydney", 1000, "No Visa", 500)
)
