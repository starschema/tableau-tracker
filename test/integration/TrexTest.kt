package com.starschema.brilliant.tableautracker.test.integration

import com.starschema.brilliant.tableautracker.API_PREFIX_1_0
import com.starschema.brilliant.tableautracker.EVENTS_PREFIX
import com.starschema.brilliant.tableautracker.test.DatabaseTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TrexTest : DatabaseTestBase("$API_PREFIX_1_0$EVENTS_PREFIX/trex") {

    @Test
    fun `trex file generation default`() {
        testRequest(HttpMethod.Get) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertNotNull(response.content, "There is no response content!")
            assertTrue(
                response.content!!.contains("<url>http://localhost/extension/index.html?backendUrl=http%3A%2F%2Flocalhost%2Fapi%2F1.0%2Fevents</url>"),
                "Response missing right url!"
            )
        }

    }

    @Test
    fun `trex file generation with url param`() {
        val fakeUrl = "https://fakeurl.net"
        testRequest(HttpMethod.Get, "$defaultUrl?url=$fakeUrl") {
            assertEquals(HttpStatusCode.OK, response.status())
            assertNotNull(response.content, "There is no response content!")
            assertTrue(
                response.content!!.contains("<url>$fakeUrl</url>"),
                "Response missing right url!"
            )
        }
    }
}