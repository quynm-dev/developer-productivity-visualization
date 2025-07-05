package com.dpv.data.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class ReleaseEntity(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<ReleaseEntity>(Releases)

    var githubUrl by Releases.githubUrl
    var repoId by Releases.repoId
    var tagName by Releases.tagName
    var branch by Releases.branch
    var name by Releases.name
    var body by Releases.body
    var draft by Releases.draft
    var githubCreatedAt by Releases.githubCreatedAt
    var githubPublishedAt by Releases.githubPublishedAt
    var createdAt by Releases.createdAt
    var updatedAt by Releases.updatedAt
}

object Releases: LongIdTable("releases") {
    val githubUrl = text("github_url")
    val repoId = reference("repo_id", Repositories)
    val tagName = text("tag_name")
    val branch = text("branch")
    val name = text("name")
    val body = text("body")
    val draft = bool("draft")
    val githubCreatedAt = datetime("github_created_at")
    val githubPublishedAt = datetime("github_published_at")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}