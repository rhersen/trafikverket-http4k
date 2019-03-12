package trafikverket

import org.http4k.core.*
import org.http4k.format.Gson.auto
import org.http4k.lens.Header

fun announcement(a: TrainAnnouncement, stations: Map<String?, List<TrainStation>>): String = """
  <tr>
    <td class="w960">${a.InformationOwner}</td>
    <td class="w960">${a.TypeOfTraffic}</td>
    <td class="w480">${a.AdvertisedTrainIdent}</td>
    <td class="w960">${a.from(stations)}</td>
    <td>${a.to(stations)}</td>
    <td class="w960">${a.via(stations)}</td>
    <td>${a.ActivityType}</td>
    <td class="w1440">${a.location(stations)}</td>
    <td class="w640">${a.TrackAtLocation}</td>
    <td>${a.advertised()}</td>
    <td>${a.estimated()}</td>
    <td>${a.actual()}</td>
    <td class="w480">${a.Canceled}</td>
    <td class="w480">${a.deviation()}</td>
    <td class="w1024">${a.other()}</td>
    <td class="w1280">${a.composition()}</td>
    <td class="w1440">${a.booking()}</td>
    <td class="w1280">${a.product()}</td>
  </tr>
"""

fun announcements(location: String?, client: HttpHandler): List<TrainAnnouncement> {
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
        e.printStackTrace()
        println(target)
        emptyList()
    }
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

