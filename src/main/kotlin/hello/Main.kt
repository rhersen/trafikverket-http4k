package hello

import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Gson.auto
import org.http4k.lens.Header
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    routes(
            "/css" bind static(Classpath("/css")),
            "favicon.ico" bind Method.GET to { Response(OK) },
            "" bind Method.GET to ::response
    ).asServer(Jetty(4001)).start()
}

const val head = "<html><head><link rel='stylesheet' type='text/css' href='css/style.css'><meta charset='UTF-8'/><body><table>"

private fun response(request: Request): Response {
    val client = JavaHttpClient()
    val stations = stations(client)
    return Response(OK)
            .header("content-type", ContentType.TEXT_HTML.value)
            .body(head + announcements(request.query("location"), client)
                    .joinToString(separator = "") { announcement(it, stations) })
}

fun announcement(a: TrainAnnouncement, stations: Map<String?, List<TrainStation>>): String = """
  <tr>
    <td>${a.InformationOwner}</td>
    <td>${a.TypeOfTraffic}</td>
    <td>${a.AdvertisedTrainIdent}</td>
    <td>${a.from(stations)}</td>
    <td>${a.to(stations)}</td>
    <td>${a.via(stations)}</td>
    <td>${a.ActivityType}</td>
    <td>${a.location(stations)}</td>
    <td>${a.TrackAtLocation}</td>
    <td>${a.advertised()}</td>
    <td>${a.estimated()}</td>
    <td>${a.actual()}</td>
    <td>${a.Canceled}</td>
    <td>${a.deviation()}</td>
    <td>${a.other()}</td>
    <td>${a.composition()}</td>
    <td>${a.booking()}</td>
    <td>${a.product()}</td>
  </tr>
"""


private fun stations(client: HttpHandler): Map<String?, List<TrainStation>> {
    var target: Response? = null

    return try {
        val readText: String? = Classpath().load("cache/stations.json")?.readText()
        target = if (readText == null) getResponse(client, stationsQuery().trimMargin())
        else Response(OK).body(readText)

        Body.auto<StationsWrapper>()
                .toLens()
                .extract(target)
                .RESPONSE
                ?.RESULT
                .orEmpty()
                .flatMap { it.TrainStation }
                .groupBy(TrainStation::LocationSignature)
    } catch (e: Exception) {
        println(e)
        println(target)
        emptyMap()
    }
}

private fun announcements(location: String?, client: HttpHandler): List<TrainAnnouncement> {
    val target: Response = getResponse(client, announcementQuery(location).trimMargin())
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

private fun getResponse(client: HttpHandler, body: String): Response {
    return client(Request(Method.POST, "http://api.trafikinfo.trafikverket.se/v1.2/data.json")
            .with(Header.CONTENT_TYPE of ContentType.APPLICATION_XML)
            .body(body))
}

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

private fun stationsQuery(): String = """<REQUEST>
     <LOGIN authenticationkey='$key' />
     <QUERY objecttype='TrainStation'>
      <FILTER>
       <OR>
         <IN name='CountyNo' value='1' />
         <EQ name='LocationSignature' value='U' />
         <EQ name='LocationSignature' value='Kn' />
         <EQ name='LocationSignature' value='Gn' />
         <EQ name='LocationSignature' value='Bål' />
       </OR>
      </FILTER>
     </QUERY>
    </REQUEST>"""