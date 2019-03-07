package trafikverket

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

fun index(): Response {
    val client = JavaHttpClient()
    val stations = stations(client)
    return Response(OK)
            .header("content-type", ContentType.TEXT_HTML.value)
            .body(head + locations(client, stations)
                    .joinToString { location(it, stations) })
}

fun location(request: Request): Response {
    val client = JavaHttpClient()
    val stations = stations(client)
    return Response(OK)
            .header("content-type", ContentType.TEXT_HTML.value)
            .body(head + announcements(request.path("location"), client)
                    .joinToString(separator = "") { announcement(it, stations) })
}

fun stations(client: HttpHandler): Map<String?, List<TrainStation>> {
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
