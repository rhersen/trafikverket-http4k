package hello

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.Test

class TrainAnnouncementTest {
    @Test
    fun `advertised, null`() =
            assertThat(
                    TrainAnnouncement().advertised(),
                    equalTo("-")
            )

    @Test
    fun `advertised, zero seconds`() =
            assertThat(
                    TrainAnnouncement(AdvertisedTimeAtLocation = "2019-02-14T17:50:00").advertised(),
                    equalTo("17:50")
            )

    @Test
    fun `advertised, non-zero seconds`() =
            assertThat(
                    TrainAnnouncement(AdvertisedTimeAtLocation = "2019-02-14T17:33:29").advertised(),
                    equalTo("17:33:29")
            )

    @Test
    fun `advertised, without date`() =
            assertThat(
                    TrainAnnouncement(AdvertisedTimeAtLocation = "17:50").advertised(),
                    equalTo("17:50")
            )

    @Test
    fun `booking, null`() =
            assertThat(
                    TrainAnnouncement().booking(),
                    equalTo("")
            )

    @Test
    fun `booking, not null`() =
            assertThat(
                    TrainAnnouncement(Booking = listOf("Vagn 7 obokad.")).booking(),
                    equalTo("Vagn 7 obokad.")
            )

    @Test
    fun `booking, more than one`() =
            assertThat(
                    TrainAnnouncement(Booking = listOf("Vagn 7 obokad.", "Vagn 8 obokad.")).booking(),
                    equalTo("Vagn 7 obokad. Vagn 8 obokad.")
            )

    @Test
    fun `location, no LocationSignature`() =
            assertThat(
                    TrainAnnouncement().location(emptyMap()),
                    equalTo("")
            )

    @Test
    fun `location, LocationSignature not found`() =
            assertThat(
                    TrainAnnouncement(LocationSignature = "Sta").location(emptyMap()),
                    equalTo("Sta")
            )

    @Test
    fun `location, LocationSignature found, but no TrainStation`() =
            assertThat(
                    TrainAnnouncement(LocationSignature = "Sta").location(mapOf("Sta" to emptyList())),
                    equalTo("Sta")
            )

    @Test
    fun `location, LocationSignature found, but empty TrainStation`() =
            assertThat(
                    TrainAnnouncement(LocationSignature = "Sta")
                            .location(mapOf("Sta" to listOf(TrainStation()))),
                    equalTo("Sta")
            )

    @Test
    fun `location, LocationSignature found`() =
            assertThat(
                    TrainAnnouncement(LocationSignature = "Sta")
                            .location(mapOf("Sta" to listOf(TrainStation(AdvertisedShortLocationName = "Stuvsta")))),
                    equalTo("Stuvsta")
            )

    @Test
    fun `via, null`() =
            assertThat(
                    TrainAnnouncement().via(),
                    equalTo("")
            )

    @Test
    fun `via, not null`() =
            assertThat(
                    TrainAnnouncement(ViaToLocation = listOf(
                            Location(LocationName = "Söö", Priority = 2, Order = 0),
                            Location(LocationName = "Vhd", Priority = 3, Order = 1),
                            Location(LocationName = "Nk", Priority = 1, Order = 2),
                            Location(LocationName = "Kon", Priority = 4, Order = 3)
                    )).via(),
                    equalTo("Nk")
            )
}