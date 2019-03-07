package trafikverket

data class AnnouncementsWrapper(val RESPONSE: AnnouncementsResultWrapper?)

data class AnnouncementsResultWrapper(val RESULT: List<AnnouncementsResult>)

data class AnnouncementsResult(val TrainAnnouncement: List<TrainAnnouncement>)
