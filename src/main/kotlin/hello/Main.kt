package hello

import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Gson.auto
import org.http4k.lens.Header
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    routes(
            "/css" bind static(Classpath("/css")),
            "favicon.ico" bind Method.GET to { Response(OK) },
            "/location/{location}" bind Method.GET to ::location,
            "/" bind Method.GET to { index() }
    ).asServer(Jetty(4001)).start()
}

const val head = "<html><head><link rel='stylesheet' type='text/css' href='/css/style.css'><meta content='true' name='HandheldFriendly'><meta content='width=device-width, height=device-height, user-scalable=no' name='viewport'><meta charset='UTF-8'/><body><table>"

private fun index(): Response {
    val client = JavaHttpClient()
    val stations = stations(client)
    return Response(OK)
            .header("content-type", ContentType.TEXT_HTML.value)
            .body(head + locations(client)
                    .joinToString { location(it, stations) })
}

private fun location(request: Request): Response {
    val client = JavaHttpClient()
    val stations = stations(client)
    return Response(OK)
            .header("content-type", ContentType.TEXT_HTML.value)
            .body(head + announcements(request.path("location"), client)
                    .joinToString(separator = "") { announcement(it, stations) })
}

fun location(a: TrainAnnouncement, stations: Map<String?, List<TrainStation>>): String {
    val locationSignature = a.LocationSignature
    return """<a href="location/$locationSignature">${a.location(stations)}</a>"""
}

fun announcement(a: TrainAnnouncement, stations: Map<String?, List<TrainStation>>): String = """
  <tr>
    <td class="w960">${a.InformationOwner}</td>
    <td class="w960">${a.TypeOfTraffic}</td>
    <td class="w480">${a.AdvertisedTrainIdent}</td>
    <td class="w640">${a.from(stations)}</td>
    <td>${a.to(stations)}</td>
    <td class="w960">${a.via(stations)}</td>
    <td>${a.ActivityType}</td>
    <td class="w960">${a.location(stations)}</td>
    <td class="w1024">${a.TrackAtLocation}</td>
    <td>${a.advertised()}</td>
    <td>${a.estimated()}</td>
    <td>${a.actual()}</td>
    <td class="w480">${a.Canceled}</td>
    <td class="w480">${a.deviation()}</td>
    <td class="w1280">${a.other()}</td>
    <td class="w1280">${a.composition()}</td>
    <td class="w1440">${a.booking()}</td>
    <td class="w1280">${a.product()}</td>
  </tr>
"""

private fun stations(client: HttpHandler): Map<String?, List<TrainStation>> {
    return try {
        Stations.stationsWrapper(client)
                .RESPONSE
                ?.RESULT
                .orEmpty()
                .flatMap { it.TrainStation }
                .groupBy(TrainStation::LocationSignature)
    } catch (e: Exception) {
        println(e)
        emptyMap()
    }
}

private fun locations(client: HttpHandler): List<TrainAnnouncement> {
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
    } catch (e: Exception) {
        println(e)
        println(target)
        emptyList()
    }
}

private fun announcements(location: String?, client: HttpHandler): List<TrainAnnouncement> {
    val target: Response = client(Request(Method.POST, "http://api.trafikinfo.trafikverket.se/v1.2/data.json")
            .with(Header.CONTENT_TYPE of ContentType.APPLICATION_XML)
            .body(announcementQuery(location).trimMargin()))
    return try {
        Body.auto<AnnouncementsWrapper>()
                .toLens()
                .extract(target)
                .RESPONSE
                ?.RESULT
                .orEmpty()
                .flatMap { it.TrainAnnouncement }
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

private fun announcementQuery(location: String?): String = """<REQUEST>
                |<LOGIN authenticationkey='$key' />
                |<QUERY objecttype="TrainAnnouncement" orderby="AdvertisedTimeAtLocation">
                |<FILTER>
                |<AND>
                |<EQ name="LocationSignature" value="${location ?: 'N'}" />
                |<GT name="AdvertisedTimeAtLocation" value="${"$"}dateadd(-00:15:00)" />
                |<LT name="AdvertisedTimeAtLocation" value="${"$"}dateadd(00:15:00)" />
                |</AND>
                |</FILTER>
                |</QUERY>
                |</REQUEST>"""

