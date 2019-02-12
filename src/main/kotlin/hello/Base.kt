package hello

data class Base(val RESPONSE: ResultWrapper?)

data class FromLocation(val LocationName: String?, val Priority: Number?, val Order: Number?)

data class ResultWrapper(val RESULT: List<Result>?)

data class Result(val TrainAnnouncement: List<TrainAnnouncement>?)

data class ToLocation(val LocationName: String?, val Priority: Number?, val Order: Number?)

data class ViaToLocation(val LocationName: String?, val Priority: Number?, val Order: Number?)
