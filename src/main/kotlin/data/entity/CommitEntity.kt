package com.dpv.data.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class CommitEntity(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<CommitEntity>(Commits)

    var hash by Commits.hash
    var userId by Commits.userId
    var githubUrl by Commits.githubUrl
    var message by Commits.message
    var commitedAt by Commits.commitedAt
    var createdAt by Commits.createdAt
    var updatedAt by Commits.updatedAt
}

object Commits: LongIdTable("commits") {
    val hash = varchar("hash", 255).uniqueIndex()
    val userId = reference("user_id", Users)
    val githubUrl = text("github_url")
    val message = text("message")
    val commitedAt = datetime("committed_at")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}