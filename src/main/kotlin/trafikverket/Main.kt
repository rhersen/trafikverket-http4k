package trafikverket

import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Jetty
import org.http4k.server.asServer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    routes(
            "/css" bind static(Classpath("/css")),
            "favicon.ico" bind Method.GET to { Response(OK) },
            "/location/{location}" bind Method.GET to ::location,
            "/" bind Method.GET to { index() }
    ).asServer(Jetty(4001)).start()
}

private const val head = "<html>" +
        "<head>" +
        "<link rel='stylesheet' type='text/css' href='/css/style.css'>" +
        "<meta content='true' name='HandheldFriendly'>" +
        "<meta content='width=device-width, height=device-height, user-scalable=no' name='viewport'>" +
        "<meta charset='UTF-8'/>" +
        "<body>"

private fun index(): Response {
    val client = JavaHttpClient()
    val stations = stations(client)
    return Response(OK)
            .header("content-type", ContentType.TEXT_HTML.value)
            .body(head + locations(client, stations)
                    .joinToString { location(it, stations) })
}

private fun location(request: Request): Response {
    val client = JavaHttpClient()
    val stations = stations(client)
    val announcements = announcements(request.path("location"), client)
    return Response(OK)
            .header("content-type", ContentType.TEXT_HTML.value)
            .body(head +
                    station(announcements, stations) +
                    columnHeadings() +
                    announcements
                            .filter { it.ProductInformation.isNotEmpty() }
                            .joinToString(separator = "") { announcement(it, stations) })
}

private fun station(announcements: List<TrainAnnouncement>, stations: Map<String?, List<TrainStation>>): String {
    fun location(announcement: TrainAnnouncement): String {
        val locationSignature = announcement.LocationSignature
        return stations[locationSignature]
                .orEmpty()
                .joinToString {
                    "<div><span>Avgående tåg - ${
                    it.AdvertisedLocationName
                    }</span><span>${
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                    }</span></div>"
                }
                .ifEmpty { locationSignature ?: "" }
    }

    return announcements
            .distinctBy { it.LocationSignature }
            .joinToString(transform = ::location)
}

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
