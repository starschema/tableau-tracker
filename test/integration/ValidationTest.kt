package com.starschema.brilliant.tableautracker.test.integration

import com.starschema.brilliant.tableautracker.API_PREFIX_1_0
import com.starschema.brilliant.tableautracker.EVENTS_PREFIX
import com.starschema.brilliant.tableautracker.db.DbUtils
import com.starschema.brilliant.tableautracker.model.DeploymentEntity
import com.starschema.brilliant.tableautracker.test.DatabaseTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class ValidationTest : DatabaseTestBase("$API_PREFIX_1_0$EVENTS_PREFIX/validate") {

    lateinit var testDeployment: DeploymentEntity

    @Before
    fun init() {
        loadConfig()
        testDeployment = DbUtils.exec {
            DeploymentEntity.new {
                deploymentId = UUID.randomUUID().toString()
                firstName = "dummy"
                lastName = "dumdum"
                organization = "Dummy inc"
                jobTitle = "Dummy job"
                email = "dummy@example.com"
                adminKey = UUID.randomUUID().toString()
                optInForUpdates = true
            }
        }
    }

    @Test
    fun `valid deploymentid and adminkey`() {
        testRequest(
            HttpMethod.Post,
            setup = bodyFromFile(
                "validateReq.json",
                mapOf("deploymentId" to testDeployment.deploymentId, "adminKey" to testDeployment.adminKey)
            )
        ) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("OK", response.content)
        }
    }

    @Test
    fun `valid deploymentid wrong adminkey`() {
        testRequest(
            HttpMethod.Post,
            setup = bodyFromFile(
                "validateReq.json",
                mapOf("deploymentId" to testDeployment.deploymentId, "adminKey" to "fakeAdminKey")
            )
        ) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertEquals("DeploymentId or AdminKey is incorrect!", response.content)
        }
    }

    @Test
    fun `wrong deploymentid valid adminkey`() {
        testRequest(
            HttpMethod.Post,
            setup = bodyFromFile(
                "validateReq.json",
                mapOf("deploymentId" to "fakeDeploymentId", "adminKey" to testDeployment.adminKey)
            )
        ) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertEquals("DeploymentId or AdminKey is incorrect!", response.content)
        }
    }

    @Test
    fun `wrong deploymentid wrong adminkey`() {
        testRequest(
            HttpMethod.Post,
            setup = bodyFromFile(
                "validateReq.json",
                mapOf("deploymentId" to "fakeDeploymentId", "adminKey" to "fakeAdminKey")
            )
        ) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertEquals("DeploymentId or AdminKey is incorrect!", response.content)
        }
    }

    @Test
    fun `valid deploymentid no adminKey`() {
        testRequest(
            HttpMethod.Post,
            setup = bodyFromFile("validateReqDeploymentOnly.json", mapOf("deploymentId" to testDeployment.deploymentId))
        ) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertEquals("OK", response.content)
        }
    }

    @Test
    fun `wrong deploymentid no adminKey`() {
        testRequest(
            HttpMethod.Post,
            setup = bodyFromFile("validateReqDeploymentOnly.json", mapOf("deploymentId" to "fakeDeploymentId"))
        ) {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertEquals("DeploymentId is incorrect!", response.content)
        }
    }

    @After
    fun afterTest() {
        DbUtils.exec { testDeployment.delete() }
    }
}