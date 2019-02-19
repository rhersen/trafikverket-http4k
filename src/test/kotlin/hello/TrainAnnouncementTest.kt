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
}