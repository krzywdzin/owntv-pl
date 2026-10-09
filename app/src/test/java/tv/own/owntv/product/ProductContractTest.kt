package tv.own.owntv.product

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProductContractTest {

    @Test
    fun `customer rail is exactly the five product sections in product order`() {
        val source = File("src/main/java/tv/own/owntv/features/shell/components/StageRail.kt").readText()
        val body = Regex("""private val StageRailOrder = listOf\(([^)]*)\)""", RegexOption.DOT_MATCHES_ALL)
            .find(source)?.groupValues?.get(1).orEmpty()
        val sections = Regex("""MainSection\.([A-Z_]+)""").findAll(body).map { it.groupValues[1] }.toList()
        assertEquals(listOf("LIVE_TV", "MOVIES", "SERIES", "SEARCH", "SETTINGS"), sections)
    }

    @Test
    fun `product package and version scheme stay independent from upstream`() {
        val build = File("build.gradle.kts").readText()
        assertTrue(build.contains("""applicationId = "pl.lesnik.tv""""))
        assertTrue(build.contains("""endsWith("-klient")"""))
    }

    @Test
    fun `updater points only at product releases`() {
        val app = File("src/main/java/tv/own/owntv/OwnTVApp.kt").readText()
        assertTrue(app.contains("""CoreBuildInfo.releaseRepo = "krzywdzin/owntv-pl""""))
        assertFalse(app.contains("""CoreBuildInfo.releaseRepo = "ahXN00/OwnTV""""))
    }

    @Test
    fun `product name overrides the upstream application label`() {
        val brand = File("src/main/res/values/product_brand.xml").readText()
        assertTrue(brand.contains("""name="app_name" translatable="false">TV Leśnik</string>"""))
    }

    @Test
    fun `release cannot silently fall back to the emulator activation endpoint`() {
        val build = File("build.gradle.kts").readText()
        val releaseFallbacks = Regex("""buildTypes\s*\{([\s\S]*?)\n\s*\}""").find(build)?.value.orEmpty()
        assertTrue(build.contains("http://10.0.2.2:8787"))
        assertTrue(build.contains("if (activationBaseUrl.isBlank())"))
        // The fallback lives in debug; release builds get only the explicit build input.
        assertTrue(build.indexOf("http://10.0.2.2:8787") > build.indexOf("debug {"))
        assertTrue(build.indexOf("http://10.0.2.2:8787") < build.indexOf("release {"))
        assertFalse(releaseFallbacks.contains("192.168."))
    }
}
