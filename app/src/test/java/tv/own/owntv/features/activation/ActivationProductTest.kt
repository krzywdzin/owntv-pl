package tv.own.owntv.features.activation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivationProductTest {

    @Test
    fun `activation code is normalized for a remote keyboard`() {
        assertEquals("AB12CD34", ActivationViewModel.normalize("ab-12 cd_34"))
        assertEquals("12345678", ActivationViewModel.normalize("1234567890"))
    }

    @Test
    fun `xtream base url is assembled from activation payload`() {
        val secure = ActivationPayload(
            server = "example.test",
            port = 443,
            https = true,
            username = "u",
            password = "p",
            expires = null,
            supportPhone = null,
        )
        assertEquals("https://example.test:443", secure.xtreamBaseUrl())

        val alreadyQualified = secure.copy(server = "https://panel.example.test", port = 8443)
        assertEquals("https://panel.example.test", alreadyQualified.xtreamBaseUrl())
    }

    @Test
    fun `subscription expiry accepts backend and xtream timestamp shapes`() {
        val now = 1_800_000_000_000L

        assertTrue(subscriptionExpired("1700000000", now))
        assertFalse(subscriptionExpired("1900000000", now))

        assertTrue(subscriptionExpired("1700000000000", now))
        assertFalse(subscriptionExpired("1900000000000", now))

        assertTrue(subscriptionExpired("2023-11-14T22:13:20Z", now))
        assertFalse(subscriptionExpired("2030-03-17T17:46:40Z", now))

        assertFalse(subscriptionExpired(null, now))
        assertFalse(subscriptionExpired("", now))
        assertFalse(subscriptionExpired("not-a-date", now))
    }
}
