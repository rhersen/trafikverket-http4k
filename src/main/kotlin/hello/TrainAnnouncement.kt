package hello

data class Location(val LocationName: String?, val Priority: Int?, val Order: Int?)

data class TrainAnnouncement(
        val ActivityId: String? = null,
        val ActivityType: String? = null,
        val Advertised: Boolean? = null,
        val AdvertisedTimeAtLocation: String? = null,
        val AdvertisedTrainIdent: String? = null,
        val Booking: List<String> = emptyList(),
        val Canceled: Boolean? = null,
        val Deviation: List<String> = emptyList(),
        val EstimatedTimeAtLocation: String? = null,
        val EstimatedTimeIsPreliminary: Boolean? = null,
        val FromLocation: List<Location>? = null,
        val InformationOwner: String? = null,
        val LocationSignature: String? = null,
        val MobileWebLink: String? = null,
        val ModifiedTime: String? = null,
        val NewEquipment: Int? = null,
        val OtherInformation: List<String> = emptyList(),
        val PlannedEstimatedTimeAtLocationIsValid: Boolean? = null,
        val ProductInformation: List<String> = emptyList(),
        val ScheduledDepartureDateTime: String? = null,
        val TechnicalTrainIdent: String? = null,
        val TimeAtLocation: String? = null,
        val ToLocation: List<Location>? = null,
        val TrackAtLocation: String? = null,
        val TrainComposition: List<String> = emptyList(),
        val TypeOfTraffic: String? = null,
        val ViaToLocation: List<Location>? = null,
        val WebLink: String? = null
) {
    fun advertised() = time(AdvertisedTimeAtLocation)
    fun estimated() = time(EstimatedTimeAtLocation)
    fun actual() = time(TimeAtLocation)
    fun booking() = Booking.joinToString(" ")
    fun composition() = TrainComposition.joinToString("<br>")
    fun deviation() = Deviation.joinToString("<br>")
    fun other() = OtherInformation.joinToString("<br>")
    fun product() = ProductInformation.joinToString("<br>")
    fun location(stations: Map<String?, List<TrainStation>>): String = location(LocationSignature, stations)
    fun from(stations: Map<String?, List<TrainStation>>) = location(FromLocation, stations)
    fun to(stations: Map<String?, List<TrainStation>>) = location(ToLocation, stations)
    fun via(stations: Map<String?, List<TrainStation>>) = location(ViaToLocation, stations)

    private fun time(t: String?): String? = when {
        t == null -> "-"
        t.length < 16 -> t
        t.substring(16) == ":00" -> t.substring(11, 16)
        else -> t.substring(11)
    }

    private fun location(locations: List<Location>?, stations: Map<String?, List<TrainStation>>): String =
            location(
                    locations.orEmpty().minBy { it.Priority ?: 9 }?.LocationName,
                    stations
            )

    private fun location(locationSignature: String?, stations: Map<String?, List<TrainStation>>): String {
        return stations[locationSignature]
                .orEmpty()
                .mapNotNull { it.AdvertisedShortLocationName }
                .joinToString()
                .ifEmpty { locationSignature ?: "" }
    }

    fun north(stations: Map<String?, List<TrainStation>>): String? =
            stations[LocationSignature]
                    .orEmpty()
                    .mapNotNull { it.Geometry }
                    .mapNotNull { it.SWEREF99TM }
                    .map { it.substring(14) }
                    .first()
}
