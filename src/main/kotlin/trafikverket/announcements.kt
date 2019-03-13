package trafikverket

import org.http4k.core.*
import org.http4k.format.Gson.auto
import org.http4k.lens.Header

fun columnHeadings(): String = """
  <tr class="headings">
    <td class="w960">produkt</td>
    <td class="w1440">ägare</td>
    <td class="w960">typ</td>
    <td class="w480">id</td>
    <td class="w960">från</td>
    <td>till</td>
    <td class="w960">via</td>
    <td>typ</td>
    <td class="w1440">station</td>
    <td class="w640">spår</td>
    <td>tab</td>
    <td>sen</td>
    <td>vrk</td>
    <td class="w480">inst</td>
    <td class="w480">avvikelse</td>
    <td class="w1024">övrigt</td>
    <td class="w1280">ordning</td>
    <td class="w1440">bokning</td>
  </tr>
"""

fun announcement(a: TrainAnnouncement, stations: Map<String?, List<TrainStation>>): String = """
  <tr>
    <td class="w960">${a.product()}</td>
    <td class="w1440">${a.InformationOwner}</td>
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

