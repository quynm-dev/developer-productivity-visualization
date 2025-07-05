package com.dpv.data.entity

import com.dpv.data.enum.GithubState
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class MilestoneEntity(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<MilestoneEntity>(Milestones)

    var githubUrl by Milestones.githubUrl
    var state by Milestones.state
    var title by Milestones.title
    var description by Milestones.description
    var repoId by Milestones.repoId
    var openIssuesCount by Milestones.openIssuesCount
    var closedIssuesCount by Milestones.closedIssuesCount
    var closedAt by Milestones.closedAt
    var dueOn by Milestones.dueOn
    var githubCreatedAt by Milestones.githubCreatedAt
    var githubUpdatedAt by Milestones.githubUpdatedAt
    var createdAt by Milestones.createdAt
    var updatedAt by Milestones.updatedAt
}

object Milestones: LongIdTable("milestones") {
    val githubUrl = text("github_url")
    val state = enumerationByName("state", 255, GithubState::class)
    val title = text("title")
    val description = text("description")
    val repoId = reference("repo_id", Repositories)
    val userId = reference("user_id", Users)
    val openIssuesCount = integer("open_issues_count")
    val closedIssuesCount = integer("closed_issues_count")
    val closedAt = datetime("closed_at").nullable().default(null)
    val dueOn = datetime("due_on").nullable().default(null)
    val githubCreatedAt = datetime("github_created_at")
    val githubUpdatedAt = datetime("github_updated_at")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}