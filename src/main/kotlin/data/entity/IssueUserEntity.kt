package com.dpv.data.entity

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

class IssueUserEntity(id: EntityID<Long>): LongEntity(id) {
    companion object : LongEntityClass<IssueUserEntity>(IssueUsers)

    var issueId by IssueUsers.issueId
    var userId by IssueUsers.userId
    var createdAt by IssueUsers.createdAt
    var updatedAt by IssueUsers.updatedAt
}

object IssueUsers: LongIdTable("issue_users") {
    val issueId = reference("issue_id", Issues)
    val userId = reference("user_id", Users)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}
