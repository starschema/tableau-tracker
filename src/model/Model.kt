package com.starschema.brilliant.tableautracker.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*

/**
 * Registration
 */
data class RegisterReq(
    @JsonProperty("first-name")
    val firstName: String?,
    @JsonProperty("last-name")
    val lastName: String?,
    val organization: String?,
    @JsonProperty("job-title")
    val jobTitle: String?,
    val email: String,
    val optInForUpdates: Boolean
)

data class RegisterRes(
    val deploymentId: String = UUID.randomUUID().toString(),
    val adminKey: String = UUID.randomUUID().toString()
)

/**
 * Validation
 */
data class ValidationReq(
    val deploymentId: String,
    val adminKey: String?
)

/**
 * Tableau events
 */
data class TableauEvents(val events: List<TableauEvent>)

data class TableauEvent(
    val deploymentId: String,
    val sourceId: String,
    val sourceSequenceId: Int,
    val kind: EventKind,
    val workbookName: String,
    val dashboardName: String,
    val recordedAt: String,
    val data: ObjectNode?
)

class EventsQueryParams(val deploymentId: String, val adminKey: String, val workbookName: String?, val offset: Int?, val limit: Int?)