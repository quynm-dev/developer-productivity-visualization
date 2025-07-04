package com.dpv.data.entity

import com.dpv.data.enum.PullStatus
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class PullEntity(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<PullEntity>(Pulls)

    var githubUrl by Pulls.githubUrl
    var state by Pulls.state
    var title by Pulls.title
    var userId by Pulls.userId
    var repoId by Pulls.repoId
    var closedAt by Pulls.closedAt
    var mergedAt by Pulls.mergedAt
    var githubCreatedAt by Pulls.githubCreatedAt
    var githubUpdatedAt by Pulls.githubUpdatedAt
    var createdAt by Pulls.createdAt
    var updatedAt by Pulls.updatedAt
}

object Pulls: LongIdTable("pulls") {
    val githubUrl = text("github_url")
    val state = enumerationByName("state", 255, PullStatus::class)
    val title = text("title")
    val userId = reference("user_id", Users)
    val repoId = reference("repo_id", Repositories)
    val closedAt = datetime("closed_at").nullable().default(null)
    val mergedAt = datetime("merged_at").nullable().default(null)
    val githubCreatedAt = datetime("github_created_at")
    val githubUpdatedAt = datetime("github_updated_at")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}