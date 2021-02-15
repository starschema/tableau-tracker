package com.starschema.brilliant.tableautracker.test.integration

import com.google.gson.Gson
import com.starschema.brilliant.tableautracker.API_PREFIX_1_0
import com.starschema.brilliant.tableautracker.EVENTS_PREFIX
import com.starschema.brilliant.tableautracker.db.DbUtils
import com.starschema.brilliant.tableautracker.model.*
import com.starschema.brilliant.tableautracker.test.DatabaseTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.deleteAll
import org.joda.time.DateTime
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.util.*
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GetEventsTest : DatabaseTestBase("$API_PREFIX_1_0$EVENTS_PREFIX/tableau-events") {

    companion object {
        private const val testEventCount = 20
        private const val testDeploymentCount = 3
        private lateinit var testDeployments: List<DeploymentEntity>
        private lateinit var testEvents: List<EventEntity>

        @BeforeClass
        @JvmStatic
        fun setup() {
            loadConfig()
            testDeployments = DbUtils.exec {
                (1..testDeploymentCount).map {
                    DeploymentEntity.new {
                        deploymentId = UUID.randomUUID().toString()
                        firstName = "dummy${Random.nextInt()}"
                        lastName = "dumdum${Random.nextInt()}"
                        organization = "Dummy inc ${Random.nextInt()}"
                        jobTitle = "Dummy job ${Random.nextInt()}"
                        email = "dummy${Random.nextInt()}@example.com"
                        adminKey = UUID.randomUUID().toString()
                        optInForUpdates = Random.nextBoolean()
                    }
                }
            }

            testEvents = DbUtils.exec {
                testDeployments.map { testDeployment ->
                    (1..testEventCount).map {
                        EventEntity.new {
                            kind = EventKind.values()[Random.nextInt(EventKind.values().size)]
                            sourceId = UUID.randomUUID().toString()
                            sourceSequenceId = Random.nextInt()
                            data = null
                            recordedAt = DateTime.now()
                            workbookName = "Workbook${Random.nextInt(2)}"
                            dashboardName = "Dashboard${Random.nextInt()}"
                            deployment = testDeployment
                        }
                    }
                }.flatten()
            }
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            DbUtils.exec {
                EventTable.deleteAll()
                DeploymentTable.deleteAll()
            }
        }
    }

    @Test
    fun `get all events`() {
        testDeployments.forEach { testDeployment ->
            testRequest(
                HttpMethod.Get,
                "$defaultUrl?deploymentId=${testDeployment.deploymentId}&adminKey=${testDeployment.adminKey}"
            ) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertNotNull(response.content, "There is no response content!")
                val responseEvents = Gson().fromJson(response.content, TableauEvents::class.java)
                assertEquals(testEventCount, responseEvents.events.size)
            }
        }
    }

    @Test
    fun `wrong deploymentId`() {
        val testDeployment = testDeployments.first()
        testRequest(HttpMethod.Get, "$defaultUrl?deploymentId=fakeDeploymentId&adminKey=${testDeployment.adminKey}") {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertEquals("DeploymentId or AdminKey is incorrect!", response.content)
        }
    }

    @Test
    fun `without deploymentId`() {
        val testDeployment = testDeployments.first()
        testRequest(HttpMethod.Get, "$defaultUrl?adminKey=${testDeployment.adminKey}") {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals("Query param 'deploymentId' is mandatory!", response.content)
        }
    }

    @Test
    fun `wrong adminkey`() {
        val testDeployment = testDeployments.first()
        testRequest(HttpMethod.Get, "$defaultUrl?deploymentId=${testDeployment.deploymentId}&adminKey=fakeAdminKey") {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
            assertEquals("DeploymentId or AdminKey is incorrect!", response.content)
        }
    }

    @Test
    fun `without adminKey`() {
        val testDeployment = testDeployments.random()
        testRequest(HttpMethod.Get, "$defaultUrl?deploymentId=${testDeployment.deploymentId}") {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals("Query param 'adminKey' is mandatory!", response.content)
        }
    }

    @Test
    fun `valid workbook`() {
        val testEvent = testEvents.random()
        val testDeployment = DbUtils.exec { testEvent.deployment }
        val deploymentEvents =
            DbUtils.exec { testDeployment.events.filter { testEvent.workbookName == it.workbookName } }
        testRequest(
            HttpMethod.Get,
            "$defaultUrl?deploymentId=${testDeployment.deploymentId}&adminKey=${testDeployment.adminKey}&workbookName=${testEvent.workbookName}"
        ) {
            assertEquals(HttpStatusCode.OK, response.status())
            val responseEvents = Gson().fromJson(response.content, TableauEvents::class.java).events
            assertEquals(deploymentEvents.size, responseEvents.size)
            responseEvents.forEach {
                assertEquals(testEvent.workbookName, it.workbookName)
                assertEquals(testDeployment.deploymentId, it.deploymentId)
            }
        }
    }

    @Test
    fun `wrong workbook`() {
        val testDeployment = testDeployments.random()
        testRequest(
            HttpMethod.Get,
            "$defaultUrl?deploymentId=${testDeployment.deploymentId}&adminKey=${testDeployment.adminKey}&workbookName=wrongWorkbookName"
        ) {
            assertEquals(HttpStatusCode.OK, response.status())
            val responseEvents = Gson().fromJson(response.content, TableauEvents::class.java).events
            assertEquals(0, responseEvents.size)
        }
    }

    @Test
    fun `offset without limit`() {
        val testDeployment = testDeployments.random()
        testRequest(
            HttpMethod.Get,
            "$defaultUrl?deploymentId=${testDeployment.deploymentId}&adminKey=${testDeployment.adminKey}&offset=1"
        ) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertEquals("Cannot use offset without limit parameter specified!", response.content)
        }
    }

    @Test
    fun `limit`() {
        val testDeployment = testDeployments.random()
        testRequest(
            HttpMethod.Get,
            "$defaultUrl?deploymentId=${testDeployment.deploymentId}&adminKey=${testDeployment.adminKey}&limit=${testEventCount / 2}"
        ) {
            assertEquals(HttpStatusCode.OK, response.status())
            val responseEvents = Gson().fromJson(response.content, TableauEvents::class.java).events
            assertEquals(testEventCount / 2, responseEvents.size)
        }
    }

    @Test
    fun `offset and limit`() {
        val chunkSize = 4
        val testDeployment = testDeployments.random()
        (0 until chunkSize).forEach {
            testRequest(
                HttpMethod.Get,
                "$defaultUrl?deploymentId=${testDeployment.deploymentId}&adminKey=${testDeployment.adminKey}&limit=${testEventCount / chunkSize}&offset=$it"
            ) {
                assertEquals(HttpStatusCode.OK, response.status())
                val responseEvents = Gson().fromJson(response.content, TableauEvents::class.java).events
                assertEquals(testEventCount / chunkSize, responseEvents.size)
            }
        }
    }
}