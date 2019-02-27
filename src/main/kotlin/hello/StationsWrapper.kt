package hello

data class StationsWrapper(val RESPONSE: StationsResultWrapper?)

data class Geometry(val SWEREF99TM: String?, val WGS84: String?)

data class StationsResultWrapper(val RESULT: List<StationsResult>)

data class StationsResult(val TrainStation: List<TrainStation>)

data class TrainStation(
        val Advertised: Boolean? = null,
        val AdvertisedLocationName: String? = null,
        val AdvertisedShortLocationName: String? = null,
        val CountryCode: String? = null,
        val CountyNo: List<Number> = emptyList(),
        val Geometry: Geometry? = null,
        val LocationInformationText: String? = null,
        val LocationSignature: String? = null,
        val ModifiedTime: String? = null,
        val PlatformLine: List<String> = emptyList(),
        val Prognosticated: Boolean? = null
)
