package hello

data class TrainAnnouncement(
        val ActivityId: String? = null,
        val ActivityType: String? = null,
        val Advertised: Boolean? = null,
        val AdvertisedTimeAtLocation: String? = null,
        val AdvertisedTrainIdent: String? = null,
        val Booking: List<String>? = null,
        val Canceled: Boolean? = null,
        val Deviation: List<String>? = null,
        val EstimatedTimeAtLocation: String? = null,
        val EstimatedTimeIsPreliminary: Boolean? = null,
        val FromLocation: List<FromLocation>? = null,
        val InformationOwner: String? = null,
        val LocationSignature: String? = null,
        val MobileWebLink: String? = null,
        val ModifiedTime: String? = null,
        val NewEquipment: Number? = null,
        val OtherInformation: List<String>? = null,
        val PlannedEstimatedTimeAtLocationIsValid: Boolean? = null,
        val ProductInformation: List<String>? = null,
        val ScheduledDepartureDateTime: String? = null,
        val TechnicalTrainIdent: String? = null,
        val TimeAtLocation: String? = null,
        val ToLocation: List<ToLocation>? = null,
        val TrackAtLocation: String? = null,
        val TrainComposition: List<String>? = null,
        val TypeOfTraffic: String? = null,
        val ViaToLocation: List<ViaToLocation>? = null,
        val WebLink: String? = null
) {
    fun advertised() = time(AdvertisedTimeAtLocation)
    fun estimated() = time(EstimatedTimeAtLocation)
    fun actual() = time(TimeAtLocation)
    fun booking() = Booking?.joinToString(" ") ?: ""

    private fun time(t: String?): String? {
        return if (t == null)
            "-"
        else if (t.length < 16)
            t
        else if (t.substring(16) == ":00")
            t.substring(11, 16)
        else
            t.substring(11)
    }
}
