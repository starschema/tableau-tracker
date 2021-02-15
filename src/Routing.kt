package com.starschema.brilliant.tableautracker

import com.starschema.brilliant.tableautracker.model.EventsQueryParams
import com.starschema.brilliant.tableautracker.model.ValidationReq
import com.starschema.brilliant.tableautracker.service.*
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.freemarker.respondTemplate
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondOutputStream
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.slf4j.Logger
import org.slf4j.LoggerFactory


/**
 * Defines the routing of different versions of the application
 */

const val API_PREFIX_1_0 = "/api/1.0"
const val EVENTS_PREFIX = "/events"

val log: Logger = LoggerFactory.getLogger("Routing")

fun routing_1_0(): Route.() -> Unit = {
    route("$API_PREFIX_1_0$EVENTS_PREFIX") {
        post("register") {
            call.respond(register(call.receive()))
        }
        post("tableau-events") {
            addEvents(call.receive())
            call.respond("OK")
        }
        post("validate") {
            val request = call.receive<ValidationReq>()
            mustHaveDeployment(request.deploymentId, request.adminKey)
            call.respond("OK")
        }
        get("tableau-events") {
            val req = EventsQueryParams(
                deploymentId = call.parameters["deploymentId"]
                    ?: throw IllegalArgumentException("Query param 'deploymentId' is mandatory!"),
                adminKey = call.parameters["adminKey"]
                    ?: throw IllegalArgumentException("Query param 'adminKey' is mandatory!"),
                workbookName = call.parameters["workbookName"],
                offset = call.parameters["offset"]?.toInt(),
                limit = call.parameters["limit"]?.toInt()
            )
            val query = prepareEventsQuery(req)
            call.respondOutputStream(ContentType.Application.Json, HttpStatusCode.OK) { getEventsStream(req.deploymentId, query, this) }
        }
        get("trex") {
            call.response.header("Content-Disposition", "attachment; filename=TabUsageTracker.trex")
            val url = call.parameters["url"] ?: getExtensionUrl(call.request.origin)

            call.respondTemplate(
                "TabUsageTracker.trex",
                mapOf("url" to url),
                contentType = ContentType.Application.Xml
            )
        }
    }
}
