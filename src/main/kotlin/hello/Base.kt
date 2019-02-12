package hello

data class Base(val RESPONSE: RESPONSE?)

data class RESPONSE(val RESULT: List<RESULT>?)

data class RESULT(val TrainAnnouncement: List<TrainAnnouncement>?)

data class ToLocation(val LocationName: String?, val Priority: Number?, val Order: Number?)

data class TrainAnnouncement(
        val AdvertisedTimeAtLocation: String?,
        val AdvertisedTrainIdent: String?,
        val ToLocation: List<ToLocation>?,
        val TrackAtLocation: String?
)

fun TrainAnnouncement.advertised(): String {
    return AdvertisedTimeAtLocation?.substring(11, 16) ?: "-"
}
