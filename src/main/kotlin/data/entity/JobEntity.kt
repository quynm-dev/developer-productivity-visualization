package com.dpv.data.entity

import com.dpv.data.enum.JobStatus
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class JobEntity(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<JobEntity>(Jobs)

    var repositoryName by Jobs.repositoryName
    var status by Jobs.status
    var description by Jobs.description
    var lastRunAt by Jobs.lastRunAt
    var failedCount by Jobs.failedCount
    var createdAt by Jobs.createdAt
    var updatedAt by Jobs.updatedAt
}

object Jobs: IntIdTable("jobs") {
    val repositoryName = text("repository_name")
    val status = enumerationByName("status", 255, JobStatus::class)
    val description = text("description").nullable().default(null)
    val lastRunAt = datetime("last_run_at").nullable()
    val failedCount = integer("failed_count").default(0)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}