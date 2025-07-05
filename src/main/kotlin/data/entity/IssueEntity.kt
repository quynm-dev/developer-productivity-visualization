package com.dpv.data.entity

import com.dpv.data.enum.GithubState
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class IssueEntity(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<IssueEntity>(Issues)

    var githubUrl by Issues.githubUrl
    var repoId by Issues.repoId
    var milestoneId by Issues.milestoneId
    var state by Issues.state
    var title by Issues.title
    var body by Issues.body
    var closedAt by Issues.closedAt
    var createdAt by Issues.createdAt
    var updatedAt by Issues.updatedAt
}

object Issues: LongIdTable("issues") {
    val githubUrl = text("github_url")
    val repoId = reference("repo_id", Repositories)
    val milestoneId = reference("milestone_id", Milestones).nullable().default(null)
    val state = enumerationByName("state", 255, GithubState::class)
    val title = text("title")
    val body = text("body").nullable().default(null)
    val closedAt = datetime("closed_at").nullable().default(null)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}