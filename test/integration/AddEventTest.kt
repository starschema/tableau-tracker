package com.starschema.brilliant.tableautracker.test.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.starschema.brilliant.tableautracker.API_PREFIX_1_0
import com.starschema.brilliant.tableautracker.EVENTS_PREFIX
import com.starschema.brilliant.tableautracker.db.DbUtils
import com.starschema.brilliant.tableautracker.model.DeploymentEntity
import com.starschema.brilliant.tableautracker.model.EventEntity
import com.starschema.brilliant.tableautracker.model.EventKind
import com.starschema.brilliant.tableautracker.model.EventTable
import com.starschema.brilliant.tableautracker.test.DatabaseTestBase
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.deleteAll
import org.joda.time.DateTime
import org.junit.After
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AddEventTest : DatabaseTestBase("$API_PREFIX_1_0$EVENTS_PREFIX/tableau-events") {
    companion object {
        private lateinit var testDeployment: DeploymentEntity

        @BeforeClass
        @JvmStatic
        fun setup() {
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

        @AfterClass
        @JvmStatic
        fun teardown() {
            if (useMockDatabase) loadConfig()
            DbUtils.exec { testDeployment.delete() }
        }
    }

    @Test
    fun `add proper events`() {
        testRequest(
            HttpMethod.Post,
            setup = bodyFromFile("eventReqOk.json", mapOf("deploymentId" to testDeployment.deploymentId))
        ) {
            assertEquals(HttpStatusCode.OK, response.status())
            assertNotNull(response.content, "There is no response content!")
            assertEquals("OK", response.content)
            DbUtils.exec {
                assertEquals(2, EventEntity.all().count(), "Not all events persisted in the database!")
                EventEntity.all().forEach {
                    val dataAsMap = ObjectMapper().readValue(it.data, LinkedHashMap::class.java)
                    if (it.kind == EventKind.FILTER_CHANGE) {
                        assertEquals("HUNT IT DOWN", (dataAsMap as LinkedHashMap<String, String>).get("sheet"))
                        assertEquals("categorical", (dataAsMap as LinkedHashMap<String, String>).get("filterType"))
                        assertEquals("Size", (dataAsMap as LinkedHashMap<String, String>).get("fieldName"))

                    } else {
                        assertEquals(EventKind.NOOP, it.kind)
                    }
                    assertEquals("Dashboard 1", it.dashboardName)
                    assertEquals(testDeployment.deploymentId, it.deployment.deploymentId)
                    assertEquals(DateTime.parse("2018-12-03T21:58:15.947Z").millis, it.recordedAt.millis)
                    assertEquals("pjg6-uo92-mkbp-eomq-ihs2-aisj", it.sourceId)
                    assertEquals("Superstore", it.workbookName)
                    assertEquals(15, it.sourceSequenceId)
                    assertEquals(
                        "https://tableau-tracker.starschema.com/extension/",
                        (dataAsMap as LinkedHashMap<String, LinkedHashMap<String, String>>).get("document")?.get("location")
                    )
                    assertEquals(
                        "NULL",
                        (dataAsMap as LinkedHashMap<String, LinkedHashMap<String, String>>).get("document")?.get("referer")
                    )
                    assertEquals(
                        876 as Integer,
                        (dataAsMap as LinkedHashMap<String, LinkedHashMap<String, Integer>>).get("window")?.get("height")
                    )
                    assertEquals(
                        1616 as Integer,
                        (dataAsMap as LinkedHashMap<String, LinkedHashMap<String, Integer>>).get("window")?.get("width")
                    )
                }
            }
        }
    }

    @Test
    fun `event without deployment error`() {
        testRequest(
            HttpMethod.Post,
            setup = bodyFromFile("eventReqOk.json", mapOf("deploymentId" to "bad_deployment_id"))
        ) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertNotNull(response.content, "There is no response content!")
            assertEquals("Couldn't find deployment with id: bad_deployment_id", response.content)
        }
    }

    @Test
    fun `event without required fields error`() {
        testRequest(
            HttpMethod.Post,
            setup = bodyFromFile(
                "eventReqMissingRequiredError.json",
                mapOf("deploymentId" to testDeployment.deploymentId)
            )
        ) {
            assertEquals(HttpStatusCode.BadRequest, response.status())
            assertNotNull(response.content, "There is no response content!")
            assertTrue(
                response.content!!.startsWith("Parameter") && response.content!!.endsWith("is required!"),
                "Not a parameter error!"
            )
        }
    }

    @After
    fun truncateEvents() {
        DbUtils.exec { EventTable.deleteAll() }
    }
}