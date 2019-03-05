package hello

import org.http4k.core.*
import org.http4k.format.Gson.auto
import org.http4k.lens.Header
import org.http4k.routing.ResourceLoader

object Stations {
    fun stationsWrapper(client: HttpHandler): StationsWrapper {
        val readText: String? = ResourceLoader.Classpath().load("cache/stations.json")?.readText()

        return Body.auto<StationsWrapper>()
                .toLens()
                .extract(if (readText == null) client(Request(Method.POST, "http://api.trafikinfo.trafikverket.se/v1.2/data.json")
                        .with(Header.CONTENT_TYPE of ContentType.APPLICATION_XML)
                        .body(stationsQuery().trimMargin()))
                else Response(Status.OK).body(readText))
    }

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
}
