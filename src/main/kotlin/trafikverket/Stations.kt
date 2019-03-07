package trafikverket

import org.http4k.core.*
import org.http4k.format.Gson.auto
import org.http4k.lens.Header
import org.http4k.routing.ResourceLoader

object Stations {
    private var cached: Response? = null

    fun stationsWrapper(client: HttpHandler): StationsWrapper {
        val readText: String? = ResourceLoader.Classpath().load("cache/stations.json")?.readText()

        val target: Response = if (readText != null) {
            println("Using downloaded stations")
            Response(Status.OK).body(readText)
        } else {
            if (cached == null) {
                println("Getting stations from server")
                cached = client(Request(Method.POST, "http://api.trafikinfo.trafikverket.se/v1.2/data.json")
                        .with(Header.CONTENT_TYPE of ContentType.APPLICATION_XML)
                        .body(stationsQuery().trimMargin()))
            } else {
                println("Using cached stations")
            }

            cached!!
        }

        return Body.auto<StationsWrapper>().toLens().extract(target)
    }

    private fun stationsQuery(): String = """<REQUEST>
     <LOGIN authenticationkey='$key' />
     <QUERY objecttype='TrainStation' />
    </REQUEST>"""
}
