package com.starschema.brilliant.tableautracker.service

import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.core.JsonFactory
import com.starschema.brilliant.tableautracker.API_PREFIX_1_0
import com.starschema.brilliant.tableautracker.EVENTS_PREFIX
import com.starschema.brilliant.tableautracker.config.EVENT_QUERY_DEFAULT_LIMIT
import com.starschema.brilliant.tableautracker.config.EVENT_QUERY_DEFAULT_OFFSET
import com.starschema.brilliant.tableautracker.db.DbUtils
import com.starschema.brilliant.tableautracker.model.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.joda.time.DateTime
import java.io.OutputStream
import javax.security.auth.message.AuthException

const val BACKEND_URL_QUERY_PARAM = "backendUrl"
const val EXTENSION_PATH = "/extension/index.html"

fun register(req: RegisterReq): RegisterRes {
    val res = RegisterRes()
    DbUtils.exec {
        DeploymentEntity.new {
            deploymentId = res.deploymentId
            firstName = req.firstName
            lastName = req.lastName
            organization = req.organization
            jobTitle = req.jobTitle
            email = req.email
            adminKey = res.adminKey
            optInForUpdates = req.optInForUpdates
        }
    }
    return res
}

fun addEvents(req: TableauEvents) {
    DbUtils.exec {
        req.events.forEach {
            EventEntity.new {
                kind = it.kind
                sourceId = it.sourceId
                sourceSequenceId = it.sourceSequenceId
                data = it.data.toString()
                recordedAt = DateTime.parse(it.recordedAt)
                workbookName = it.workbookName
                dashboardName = it.dashboardName
                deployment = DeploymentEntity.find { DeploymentTable.deploymentId eq it.deploymentId }.firstOrNull()
                    ?: throw IllegalArgumentException("Couldn't find deployment with id: ${it.deploymentId}")
            }
        }
    }
}

fun prepareEventsQuery(request: EventsQueryParams): Query {
    val deploymentFk = mustHaveDeployment(request.deploymentId, request.adminKey)

    var condition: Op<Boolean> = EventTable.deployment.eq(deploymentFk)
    request.workbookName?.let {
        condition = condition and (EventTable.workbookName eq it)
    }
    val query = EventTable.select(condition)
    if (request.limit == null && request.offset != null) throw IllegalArgumentException("Cannot use offset without limit parameter specified!")
    val limit = request.limit ?: EVENT_QUERY_DEFAULT_LIMIT
    val offset = request.offset?.let { it * limit } ?: EVENT_QUERY_DEFAULT_OFFSET
    query.limit(limit, offset)
    return query
}

fun getEventsStream(deploymentId: String, query: Query, os: OutputStream) = DbUtils.exec {
    val jGenerator = JsonFactory().createGenerator(os, JsonEncoding.UTF8)
    jGenerator.writeStartObject()
    jGenerator.writeFieldName("events")
    jGenerator.writeStartArray()
    query.forEach { resultRow ->
        jGenerator.writeStartObject()
        jGenerator.writeStringField("deploymentId", deploymentId)
        jGenerator.writeStringField("sourceId", resultRow[EventTable.sourceId])
        jGenerator.writeNumberField("sourceSequenceId", resultRow[EventTable.sourceSequenceId])
        jGenerator.writeStringField("kind", resultRow[EventTable.kind].name)
        jGenerator.writeStringField("workbookName", resultRow[EventTable.workbookName])
        jGenerator.writeStringField("dashboardName", resultRow[EventTable.dashboardName])
        jGenerator.writeStringField("recordedAt", resultRow[EventTable.recordedAt].toString("yyyy/MM/dd HH:mm:ss"))
        if (resultRow[EventTable.data] != null) {
            jGenerator.writeFieldName("data")
            jGenerator.writeRawValue(resultRow[EventTable.data])
        }
        jGenerator.writeEndObject()
    }
    jGenerator.writeEndArray()
    jGenerator.writeEndObject()
    jGenerator.close()
}

fun mustHaveDeployment(deploymentId: String, adminKey: String?): Long = DbUtils.exec {
    var option = DeploymentTable.deploymentId eq deploymentId
    if (adminKey != null) option = option and (DeploymentTable.adminKey eq adminKey)
    val foundEntity = DeploymentEntity.find(option).limit(1).firstOrNull()
        ?: throw AuthException("DeploymentId ${adminKey?.let { "or AdminKey " } ?: ""}is incorrect!")
    foundEntity.id.value
}

fun getExtensionUrl(origin: RequestConnectionPoint): String {
    val parameters = ParametersBuilder()
    parameters.append(BACKEND_URL_QUERY_PARAM, getBackendUrl(origin))
    return URLBuilder(
        protocol = URLProtocol.createOrDefault(origin.scheme),
        host = origin.host,
        port = if (origin.port != 80) origin.port else DEFAULT_PORT,
        parameters = parameters,
        encodedPath = EXTENSION_PATH.encodeURLPath()
    ).buildString()
}

private fun getBackendUrl(origin: RequestConnectionPoint): String = URLBuilder(
    protocol = URLProtocol.createOrDefault(origin.scheme),
    host = origin.host,
    port = if (origin.port != 80) origin.port else DEFAULT_PORT,
    encodedPath = "$API_PREFIX_1_0$EVENTS_PREFIX".encodeURLPath()
).buildString()
