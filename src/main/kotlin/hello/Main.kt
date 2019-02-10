package hello

import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Gson.auto
import org.http4k.lens.Header
import org.http4k.server.Jetty
import org.http4k.server.asServer

data class Base(val RESPONSE: RESPONSE?)

data class RESPONSE(val RESULT: List<RESULT>?)

data class RESULT(val TrainAnnouncement: List<TrainAnnouncement>?)

data class TrainAnnouncement(
        val AdvertisedTimeAtLocation: String?,
        val AdvertisedTrainIdent: String?,
        val TrackAtLocation: String?
)

fun main() {
    ::response.asServer(Jetty(8000)).start()
}

private fun response(request: Request): Response = Response(OK)
        .header("content-type", "text/html")
        .body("<table>" + list(request.query("location"))
                ?.joinToString(separator = "", transform = ::announcement)
                .orEmpty())

fun announcement(a: TrainAnnouncement): String {
    return "<tr><td>" + (a.AdvertisedTrainIdent ?: "-") + "<td>" + (a.AdvertisedTimeAtLocation ?: "-")
}

private fun list(location: String?): List<TrainAnnouncement>? {
    return Body.auto<Base>()
            .toLens()
            .extract(JavaHttpClient()(Request(Method.POST, "http://api.trafikinfo.trafikverket.se/v1.2/data.json")
                    .with(Header.CONTENT_TYPE of ContentType.APPLICATION_XML)
                    .body(xmlBody(location).trimMargin())))
            .RESPONSE
            ?.RESULT
            ?.first()
            ?.TrainAnnouncement
}

private fun xmlBody(location: String?): String {
    return """<REQUEST>
                    |<LOGIN authenticationkey='${key}' />
                    |<QUERY objecttype="TrainAnnouncement" orderby="AdvertisedTimeAtLocation">
                    |<FILTER>
                    |<AND>
                    |<EQ name="ActivityType" value="Avgang" />
                    |<EQ name="LocationSignature" value="${location ?: 'N'}" />
                    |<AND>
                    |<GT name="AdvertisedTimeAtLocation" value="${"$"}dateadd(-00:20:00)" />
                    |<LT name="AdvertisedTimeAtLocation" value="${"$"}dateadd(00:20:00)" />
                    |</AND>
                    |</AND>
                    |</FILTER>
                    |<INCLUDE>AdvertisedTrainIdent</INCLUDE>
                    |<INCLUDE>AdvertisedTimeAtLocation</INCLUDE>
                    |<INCLUDE>TrackAtLocation</INCLUDE>
                    |</QUERY>
                    |</REQUEST>"""
}