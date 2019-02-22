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

private fun response(request: Request): Response = Response(OK)
        .header("content-type", ContentType.TEXT_HTML.value)
        .body("<html><head><link rel='stylesheet' type='text/css' href='css/style.css'><meta charset='UTF-8'/><body><table>" + announcements(request.query("location"))
                ?.joinToString(separator = "", transform = ::announcement)
                .orEmpty())

fun announcement(a: TrainAnnouncement): String = """
  <tr>
    <td>${a.AdvertisedTrainIdent}</td>
    <td>${a.TechnicalTrainIdent}</td>
    <td>${a.ActivityType}</td>
    <td>${a.from()}</td>
    <td>${a.to()}</td>
    <td>${a.via()}</td>
    <td>${a.advertised()}</td>
    <td>${a.estimated()}</td>
    <td>${a.actual()}</td>
    <td>${a.Advertised}</td>
    <td>${a.EstimatedTimeIsPreliminary}</td>
    <td>${a.PlannedEstimatedTimeAtLocationIsValid}</td>
    <td>${a.TrackAtLocation}</td>
    <td>${a.Canceled}</td>
    <td>${a.deviation()}</td>
    <td>${a.other()}</td>
    <td>${a.composition()}</td>
    <td>${a.booking()}</td>
    <td>${a.InformationOwner}</td>
    <td>${a.LocationSignature}</td>
    <td>${a.WebLink}</td>
    <td>${a.MobileWebLink}</td>
    <td>${a.TypeOfTraffic}</td>
    <td>${a.product()}</td>
    <td>${a.NewEquipment}</td>
    <td>${a.ScheduledDepartureDateTime}</td>
    <td>${a.ModifiedTime}</td>
  </tr>
"""


private fun announcements(location: String?): List<TrainAnnouncement>? {
    val target: Response = JavaHttpClient()(Request(Method.POST, "http://api.trafikinfo.trafikverket.se/v1.2/data.json")
            .with(Header.CONTENT_TYPE of ContentType.APPLICATION_XML)
            .body(xmlBody(location).trimMargin()))
    return try {
        Body.auto<Base>()
                .toLens()
                .extract(target)
                .RESPONSE
                ?.RESULT
                ?.first()
                ?.TrainAnnouncement
    } catch (e: Exception) {
        println(e)
        println(target)
        emptyList()
    }
}

private fun xmlBody(location: String?): String = """<REQUEST>
                |<LOGIN authenticationkey='$key' />
                |<QUERY objecttype="TrainAnnouncement" orderby="AdvertisedTimeAtLocation">
                |<FILTER>
                |<AND>
                |<EQ name="ActivityType" value="Avgang" />
                |<EQ name="LocationSignature" value="${location ?: 'N'}" />
                |<AND>
                |<GT name="AdvertisedTimeAtLocation" value="${"$"}dateadd(-00:30:00)" />
                |<LT name="AdvertisedTimeAtLocation" value="${"$"}dateadd(00:30:00)" />
                |</AND>
                |</AND>
                |</FILTER>
                |</QUERY>
                |</REQUEST>"""