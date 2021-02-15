package com.starschema.brilliant.tableautracker.test.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.starschema.brilliant.tableautracker.API_PREFIX_1_0
import com.starschema.brilliant.tableautracker.EVENTS_PREFIX
import com.starschema.brilliant.tableautracker.db.DbUtils
import com.starschema.brilliant.tableautracker.model.DeploymentEntity
import com.starschema.brilliant.tableautracker.model.DeploymentTable
import com.starschema.brilliant.tableautracker.model.RegisterRes
import com.starschema.brilliant.tableautracker.test.DatabaseTestBase
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.server.testing.contentType
import io.ktor.server.testing.setBody
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.junit.After
import org.junit.Test
import java.nio.charset.Charset
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class RegisterTest : DatabaseTestBase("$API_PREFIX_1_0$EVENTS_PREFIX/register") {
    private val mapper = ObjectMapper()

    @Test
    fun `register success`() {
        testRequest(HttpMethod.Post, setup = bodyFromFile("registerReqOk.json")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertNotNull(response.content, "There is no response content!")
            assertEquals(ContentType.Application.Json.withCharset(Charset.forName("utf-8")), response.contentType())
            val regResponse: RegisterRes = mapper.readValue(response.content, RegisterRes::class.java)
            assertNotNull(regResponse, "Couldn't parse response!")
            assertNotNull(regResponse.adminKey)
            assertNotNull(regResponse.deploymentId)

            DbUtils.exec {
                var foundDeployment =
                    DeploymentEntity.find(DeploymentTable.deploymentId eq regResponse.deploymentId).firstOrNull()
                assertNotNull(foundDeployment)
                assertEquals(regResponse.deploymentId, foundDeployment.deploymentId)
                assertEquals(regResponse.adminKey, foundDeployment.adminKey)
                assertEquals("dummy@dummy.net", foundDeployment.email)
                assertEquals("FirstName", foundDeployment.firstName)
                assertEquals("LastName", foundDeployment.lastName)
                assertEquals("ExampleJob", foundDeployment.jobTitle)
                assertEquals("ExampleOrganization", foundDeployment.organization)
                assertTrue(foundDeployment.optInForUpdates!!)
            }
        }
    }

    @Test
    fun `register with same email fail`() {
        testRequest(HttpMethod.Post, setup = bodyFromFile("registerReqOk.json")) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertNotNull(response.content, "There is no response content!")
        }
        testRequest(HttpMethod.Post, setup = bodyFromFile("registerReqOk.json")) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertNotNull(response.content, "There is no response content!")
            assertEquals(
                "Already exists: dummy@dummy.net",
                response.content,
                "Not a duplicate email error!"
            )
        }
    }

    @Test
    fun `register empty fail`() {
        testRequest(HttpMethod.Post, setup = { addHeader("Content-Type", "application/json"); setBody("{}") }) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertNotNull(response.content, "There is no response content!")
            assertTrue(
                response.content!!.startsWith("Parameter") && response.content!!.endsWith("is required!"),
                "Not a parameter error!"
            )
        }
    }

    @After
    fun truncateDeployments() {
        DbUtils.exec { DeploymentTable.deleteAll() }
    }
}
