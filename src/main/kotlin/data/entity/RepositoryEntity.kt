package com.dpv.data.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class RepositoryEntity(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<RepositoryEntity>(Repositories)

    var name by Repositories.name
    var githubUrl by Repositories.githubUrl
    var userId by Repositories.userId
    var language by Repositories.language
    var pullsUrl by Repositories.pullsUrl
    var commitsUrl by Repositories.commitsUrl
    var issuesUrl by Repositories.issuesUrl
    var milestonesUrl by Repositories.milestonesUrl
    var releasesUrl by Repositories.releasesUrl
    var pat by Repositories.pat
    var lastSyncAt by Repositories.lastSyncAt
    var createdAt by Repositories.createdAt
    var updatedAt by Repositories.updatedAt
}

object Repositories: LongIdTable("repositories") {
    val name = varchar("name", 255).uniqueIndex()
    val githubUrl = text("github_url")
    val userId = reference("user_id", Users)
    val language = varchar("language", 255)
    val pullsUrl = text("pulls_url")
    val commitsUrl = text("commits_url")
    val issuesUrl = text("issues_url")
    val milestonesUrl = text("milestones_url")
    val releasesUrl = text("releases_url")
    val pat = text("pat")
    val lastSyncAt = datetime("last_sync_at").nullable().default(null)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}