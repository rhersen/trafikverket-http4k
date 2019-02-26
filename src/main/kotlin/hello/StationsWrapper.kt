package hello

data class StationsWrapper(val RESPONSE: StationsResultWrapper?)

data class Geometry(val SWEREF99TM: String?, val WGS84: String?)

data class StationsResultWrapper(val RESULT: List<StationsResult>)

data class StationsResult(val TrainStation: List<TrainStation>)

data class TrainStation(
        val Advertised: Boolean?,
        val AdvertisedLocationName: String?,
        val AdvertisedShortLocationName: String?,
        val CountryCode: String?,
        val CountyNo: List<Number>,
        val Geometry: Geometry?,
        val LocationInformationText: String?,
        val LocationSignature: String?,
        val ModifiedTime: String?,
        val PlatformLine: List<String>,
        val Prognosticated: Boolean?
)
