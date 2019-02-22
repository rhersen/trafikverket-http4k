package hello

data class Base(val RESPONSE: ResultWrapper?)

data class ResultWrapper(val RESULT: List<Result>?)

data class Result(val TrainAnnouncement: List<TrainAnnouncement>?)
