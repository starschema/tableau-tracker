package com.starschema.brilliant.tableautracker.model

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.joda.time.DateTime

/**
 * Database table and entity declarations
 */

enum class EventKind{NOOP, FILTER_CHANGE, FILTER_STATE, SELECTION_CHANGE}

object DeploymentTable: LongIdTable("deployment") {
    val deploymentId: Column<String> = varchar("deployment_id", 255).uniqueIndex()
    val firstName: Column<String?> = varchar("first_name", 255).nullable()
    val lastName: Column<String?> = varchar("last_name", 255).nullable()
    val organization: Column<String?> = varchar("organization", 255).nullable()
    val jobTitle: Column<String?> = varchar("job_title", 255).nullable()
    val adminKey: Column<String> = varchar("admin_key", 255)
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val optInForUpdates: Column<Boolean?> = bool("opt_in_for_updates").default(false).nullable()
}

class DeploymentEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object: LongEntityClass<DeploymentEntity>(DeploymentTable)

    var deploymentId by DeploymentTable.deploymentId
    var firstName by DeploymentTable.firstName
    var lastName by DeploymentTable.lastName
    var organization by DeploymentTable.organization
    var jobTitle by DeploymentTable.jobTitle
    var adminKey by DeploymentTable.adminKey
    var email by DeploymentTable.email
    var optInForUpdates by DeploymentTable.optInForUpdates
    val events by EventEntity referrersOn EventTable.deployment
}

object EventTable: LongIdTable("event") {
    val kind: Column<EventKind> = enumerationByName("kind", 20, EventKind::class)
    val sourceId: Column<String> = varchar("source_id", 255)
    val sourceSequenceId: Column<Int> = integer("source_sequence_id")
    val recordedAt: Column<DateTime> = datetime("recorded_at").default(DateTime.now())
    val workbookName: Column<String> = varchar("workbook_name", 255)
    val dashboardName: Column<String> = varchar("dashboard_name", 255)
    val data: Column<String?> = text("data").nullable()
    val deployment = reference("deployment_fk", DeploymentTable)
}

class EventEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object: LongEntityClass<EventEntity>(EventTable)

    var kind by EventTable.kind
    var sourceId by EventTable.sourceId
    var sourceSequenceId by EventTable.sourceSequenceId
    var recordedAt by EventTable.recordedAt
    var workbookName by EventTable.workbookName
    var dashboardName by EventTable.dashboardName
    var data by EventTable.data
    var deployment by DeploymentEntity referencedOn EventTable.deployment
}
