package trafikverket

import org.http4k.core.*
import org.http4k.format.Gson.auto
import org.http4k.lens.Header

fun columnHeadings(): String = """
  <div class="headings">
    <div>
      <span>Tid</span>
      <span>Till</span>
      <span>Ny tid</span>
      <span class="w640">Spår</span>
      <span class="w480">Anmärkning</span>
    </div>
    <div>
      <span class="w960">Typ</span>
      <span class="w480">Id</span>
      <span class="w960">Via</span>
  </div>
"""

fun announcement(a: TrainAnnouncement, stations: Map<String?, List<TrainStation>>): String = """
  <div class="train">
    <div class="main">
      <span class="advertised">${a.advertised()}</span>
      <span class="to">${a.to(stations)}</span>
      <span class="estimated">${a.estimated()}</span>
      <span class="track">${a.TrackAtLocation}</span>
      <span class="deviation">${a.deviation()}</span>
    </div>
    <div class="details">
      <span class="id">${a.id()}</span>
      <span class="via">${a.via(stations)}</span>
    </div>
  </div>
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
                |<EQ name="LocationSignature" value="${location ?: "Cst"}" />
                |<EQ name="ActivityType" value="Avgang" />
                |<GT name="AdvertisedTimeAtLocation" value="${"$"}dateadd(-00:01:00)" />
                |<LT name="AdvertisedTimeAtLocation" value="${"$"}dateadd(00:59:00)" />
                |</AND>
                |</FILTER>
                |</QUERY>
                |</REQUEST>"""

