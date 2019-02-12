package hello

data class TrainAnnouncement(
        val ActivityId: String?,
        val ActivityType: String?,
        val Advertised: Boolean?,
        val AdvertisedTimeAtLocation: String?,
        val AdvertisedTrainIdent: String?,
        val Booking: List<String>?,
        val Canceled: Boolean?,
        val Deviation: List<String>?,
        val EstimatedTimeIsPreliminary: Boolean?,
        val FromLocation: List<FromLocation>?,
        val InformationOwner: String?,
        val LocationSignature: String?,
        val MobileWebLink: String?,
        val ModifiedTime: String?,
        val NewEquipment: Number?,
        val OtherInformation: List<String>?,
        val PlannedEstimatedTimeAtLocationIsValid: Boolean?,
        val ProductInformation: List<String>?,
        val ScheduledDepartureDateTime: String?,
        val TechnicalTrainIdent: String?,
        val ToLocation: List<ToLocation>?,
        val TrackAtLocation: String?,
        val TrainComposition: List<String>?,
        val TypeOfTraffic: String?,
        val ViaToLocation: List<ViaToLocation>?,
        val WebLink: String?
)

fun TrainAnnouncement.advertised(): String {
    return AdvertisedTimeAtLocation?.substring(11, 16) ?: "-"
}
