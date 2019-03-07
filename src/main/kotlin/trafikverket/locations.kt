package trafikverket

import org.http4k.core.*
import org.http4k.format.Gson.auto
import org.http4k.lens.Header

fun location(a: TrainAnnouncement, stations: Map<String?, List<TrainStation>>): String {
    val locationSignature = a.LocationSignature
    return """<a href="location/$locationSignature">${a.location(stations)}</a>"""
}

fun locations(client: HttpHandler, stations: Map<String?, List<TrainStation>>): List<TrainAnnouncement> {
    val target: Response = client(Request(Method.POST, "http://api.trafikinfo.trafikverket.se/v1.2/data.json")
            .with(Header.CONTENT_TYPE of ContentType.APPLICATION_XML)
            .body(locationsQuery().trimMargin()))
    return try {
        Body.auto<AnnouncementsWrapper>()
                .toLens()
                .extract(target)
                .RESPONSE
                ?.RESULT
                .orEmpty()
                .flatMap { it.TrainAnnouncement }
                .sortedBy { it.north(stations) }
                .distinctBy { it.LocationSignature }
    } catch (e: Exception) {
        println(e)
        println(target)
        emptyList()
    }
}

private fun locationsQuery(): String = """<REQUEST>
                |<LOGIN authenticationkey='$key' />
                |<QUERY objecttype="TrainAnnouncement" orderby="AdvertisedTimeAtLocation">
                |<FILTER>
                |<AND>
                |<GT name="AdvertisedTimeAtLocation" value="${"$"}dateadd(00:00:00)" />
                |<LT name="AdvertisedTimeAtLocation" value="${"$"}dateadd(00:01:00)" />
                |</AND>
                |</FILTER>
                |</QUERY>
                |</REQUEST>"""
